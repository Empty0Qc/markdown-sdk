package com.chenge.markdown.render

import android.util.Log
import android.widget.TextView
import io.noties.markwon.Markwon
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 增强的 Markdown 渲染器
 * 支持同步、异步和协程三种渲染模式
 */
class EnhancedMarkdownRenderer(
    private val config: RenderConfig = RenderConfig()
) {
    
    companion object {
        private const val TAG = "EnhancedMarkdownRenderer"
        private val instanceCounter = AtomicInteger(0)
    }
    
    private val instanceId = instanceCounter.incrementAndGet()
    private val renderCache = ConcurrentHashMap<String, CharSequence>()
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val activeFutures = ConcurrentHashMap<String, Future<*>>()
    
    /**
     * 渲染 Markdown 内容
     */
    fun render(
        markwon: Markwon,
        textView: TextView,
        markdown: String,
        listener: RenderListener? = null
    ) {
        val startTime = System.currentTimeMillis()
        val cacheKey = generateCacheKey(markdown)
        
        listener?.onRenderStart(config.mode)
        
        // 检查缓存
        if (config.enableCache && renderCache.containsKey(cacheKey)) {
            val cachedContent = renderCache[cacheKey]!!
            textView.text = cachedContent
            
            val performance = RenderPerformance(
                parseTimeMs = 0,
                renderTimeMs = 0,
                totalTimeMs = System.currentTimeMillis() - startTime,
                mode = config.mode,
                contentLength = markdown.length,
                cacheHit = true
            )
            
            listener?.onRenderComplete(
                RenderResult.Success(
                    content = cachedContent,
                    renderTimeMs = performance.totalTimeMs,
                    mode = config.mode
                )
            )
            return
        }
        
        when (config.mode) {
            RenderMode.SYNC -> renderSync(markwon, textView, markdown, listener, startTime)
            RenderMode.ASYNC -> renderAsync(markwon, textView, markdown, listener, startTime)
            RenderMode.COROUTINE -> renderCoroutine(markwon, textView, markdown, listener, startTime)
        }
    }
    
    /**
     * 同步渲染
     */
    private fun renderSync(
        markwon: Markwon,
        textView: TextView,
        markdown: String,
        listener: RenderListener?,
        startTime: Long
    ) {
        try {
            val parseStart = System.currentTimeMillis()
            val node = markwon.parse(markdown)
            val parseTime = System.currentTimeMillis() - parseStart
            
            val renderStart = System.currentTimeMillis()
            val spanned = markwon.render(node)
            val renderTime = System.currentTimeMillis() - renderStart
            
            textView.text = spanned
            
            // 缓存结果
            if (config.enableCache) {
                renderCache[generateCacheKey(markdown)] = spanned
            }
            
            val totalTime = System.currentTimeMillis() - startTime
            val performance = RenderPerformance(
                parseTimeMs = parseTime,
                renderTimeMs = renderTime,
                totalTimeMs = totalTime,
                mode = RenderMode.SYNC,
                contentLength = markdown.length
            )
            
            if (config.enablePerformanceMonitoring) {
                Log.d(TAG, "Sync render performance: $performance")
            }
            
            listener?.onRenderComplete(
                RenderResult.Success(
                    content = spanned,
                    renderTimeMs = totalTime,
                    mode = RenderMode.SYNC
                )
            )
            
        } catch (e: Exception) {
            handleRenderError(e, RenderMode.SYNC, listener)
        }
    }
    
    /**
     * 异步渲染
     */
    private fun renderAsync(
        markwon: Markwon,
        textView: TextView,
        markdown: String,
        listener: RenderListener?,
        startTime: Long
    ) {
        val taskId = generateTaskId()
        
        val future = MarkdownScheduler.asyncRender(
            backgroundTask = {
                val parseStart = System.currentTimeMillis()
                val node = markwon.parse(markdown)
                val parseTime = System.currentTimeMillis() - parseStart
                
                val renderStart = System.currentTimeMillis()
                val spanned = markwon.render(node)
                val renderTime = System.currentTimeMillis() - renderStart
                
                Triple(spanned, parseTime, renderTime)
            },
            onResult = { (spanned, parseTime, renderTime) ->
                activeFutures.remove(taskId)
                
                textView.text = spanned
                
                // 缓存结果
                if (config.enableCache) {
                    renderCache[generateCacheKey(markdown)] = spanned
                }
                
                val totalTime = System.currentTimeMillis() - startTime
                val performance = RenderPerformance(
                    parseTimeMs = parseTime,
                    renderTimeMs = renderTime,
                    totalTimeMs = totalTime,
                    mode = RenderMode.ASYNC,
                    contentLength = markdown.length
                )
                
                if (config.enablePerformanceMonitoring) {
                    Log.d(TAG, "Async render performance: $performance")
                }
                
                listener?.onRenderComplete(
                    RenderResult.Success(
                        content = spanned,
                        renderTimeMs = totalTime,
                        mode = RenderMode.ASYNC
                    )
                )
            },
            onError = { error ->
                activeFutures.remove(taskId)
                handleRenderError(error, RenderMode.ASYNC, listener, markwon, textView, markdown)
            }
        )
        
        activeFutures[taskId] = future
        
        // 设置超时
        if (config.timeoutMs > 0) {
            MarkdownScheduler.executeOnMainDelayed({
                if (activeFutures.containsKey(taskId)) {
                    future.cancel(true)
                    activeFutures.remove(taskId)
                    
                    listener?.onRenderComplete(
                        RenderResult.Cancelled(
                            mode = RenderMode.ASYNC,
                            reason = "Timeout after ${config.timeoutMs}ms"
                        )
                    )
                }
            }, config.timeoutMs)
        }
    }
    
    /**
     * 协程渲染
     */
    private fun renderCoroutine(
        markwon: Markwon,
        textView: TextView,
        markdown: String,
        listener: RenderListener?,
        startTime: Long
    ) {
        val taskId = generateTaskId()
        
        val job = CoroutineScope(config.coroutineDispatcher).launch {
            try {
                val parseStart = System.currentTimeMillis()
                val node = markwon.parse(markdown)
                val parseTime = System.currentTimeMillis() - parseStart
                
                ensureActive() // 检查是否被取消
                
                val renderStart = System.currentTimeMillis()
                val spanned = markwon.render(node)
                val renderTime = System.currentTimeMillis() - renderStart
                
                ensureActive() // 再次检查是否被取消
                
                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    activeJobs.remove(taskId)
                    
                    textView.text = spanned
                    
                    // 缓存结果
                    if (config.enableCache) {
                        renderCache[generateCacheKey(markdown)] = spanned
                    }
                    
                    val totalTime = System.currentTimeMillis() - startTime
                    val performance = RenderPerformance(
                        parseTimeMs = parseTime,
                        renderTimeMs = renderTime,
                        totalTimeMs = totalTime,
                        mode = RenderMode.COROUTINE,
                        contentLength = markdown.length
                    )
                    
                    if (config.enablePerformanceMonitoring) {
                        Log.d(TAG, "Coroutine render performance: $performance")
                    }
                    
                    listener?.onRenderComplete(
                        RenderResult.Success(
                            content = spanned,
                            renderTimeMs = totalTime,
                            mode = RenderMode.COROUTINE
                        )
                    )
                }
                
            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    activeJobs.remove(taskId)
                    listener?.onRenderComplete(
                        RenderResult.Cancelled(
                            mode = RenderMode.COROUTINE,
                            reason = "Coroutine cancelled"
                        )
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    activeJobs.remove(taskId)
                    handleRenderError(e, RenderMode.COROUTINE, listener, markwon, textView, markdown)
                }
            }
        }
        
        activeJobs[taskId] = job
        
        // 设置超时
        if (config.timeoutMs > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(config.timeoutMs)
                if (activeJobs.containsKey(taskId)) {
                    job.cancel()
                    activeJobs.remove(taskId)
                    
                    listener?.onRenderComplete(
                        RenderResult.Cancelled(
                            mode = RenderMode.COROUTINE,
                            reason = "Timeout after ${config.timeoutMs}ms"
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 处理渲染错误
     */
    private fun handleRenderError(
        error: Throwable,
        mode: RenderMode,
        listener: RenderListener?,
        markwon: Markwon? = null,
        textView: TextView? = null,
        markdown: String? = null
    ) {
        Log.e(TAG, "Render error in $mode mode", error)
        
        when (config.errorHandling) {
            ErrorHandling.FALLBACK_TO_SYNC -> {
                if (mode != RenderMode.SYNC && markwon != null && textView != null && markdown != null) {
                    try {
                        renderSync(markwon, textView, markdown, listener, System.currentTimeMillis())
                        return
                    } catch (fallbackError: Exception) {
                        Log.e(TAG, "Fallback sync render also failed", fallbackError)
                    }
                }
                listener?.onRenderComplete(
                    RenderResult.Error(
                        exception = error,
                        mode = mode,
                        fallbackContent = "渲染失败，已尝试降级处理"
                    )
                )
            }
            
            ErrorHandling.SHOW_ERROR -> {
                listener?.onRenderComplete(
                    RenderResult.Error(
                        exception = error,
                        mode = mode,
                        fallbackContent = "渲染错误: ${error.message}"
                    )
                )
            }
            
            ErrorHandling.SILENT_FAIL -> {
                listener?.onRenderComplete(
                    RenderResult.Error(
                        exception = error,
                        mode = mode,
                        fallbackContent = ""
                    )
                )
            }
            
            ErrorHandling.THROW_EXCEPTION -> {
                throw error
            }
        }
    }
    
    /**
     * 取消所有活动的渲染任务
     */
    fun cancelAll() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        
        activeFutures.values.forEach { it.cancel(true) }
        activeFutures.clear()
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        renderCache.clear()
    }
    
    /**
     * 获取缓存统计
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            size = renderCache.size,
            enabled = config.enableCache
        )
    }
    
    /**
     * 获取活动任务统计
     */
    fun getActiveTaskStats(): ActiveTaskStats {
        return ActiveTaskStats(
            coroutineJobs = activeJobs.size,
            asyncFutures = activeFutures.size
        )
    }
    
    private fun generateCacheKey(markdown: String): String {
        return "${markdown.hashCode()}_${config.hashCode()}"
    }
    
    private fun generateTaskId(): String {
        return "task_${instanceId}_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
    }
    
    /**
     * 缓存统计
     */
    data class CacheStats(
        val size: Int,
        val enabled: Boolean
    )
    
    /**
     * 活动任务统计
     */
    data class ActiveTaskStats(
        val coroutineJobs: Int,
        val asyncFutures: Int
    )
}