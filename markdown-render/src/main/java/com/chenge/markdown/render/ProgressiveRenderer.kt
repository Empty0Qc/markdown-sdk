package com.chenge.markdown.render

import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.widget.TextView
import android.widget.ScrollView
import android.widget.NestedScrollView
import androidx.core.widget.NestedScrollView
import io.noties.markwon.Markwon
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 渐进式渲染器 - 支持逐字符、逐段落、流式追加等渲染模式
 * 
 * 目标：支持逐字符展示、块级展示、流式内容 append，便于 AI 助手使用
 */
class ProgressiveRenderer(
    private val markwon: Markwon,
    private val target: TextView,
    private val scrollView: ScrollView? = null,
    private val nestedScrollView: NestedScrollView? = null
) {
    
    companion object {
        private const val DEFAULT_CHAR_DELAY_MS = 50L
        private const val DEFAULT_BLOCK_DELAY_MS = 200L
        private const val DEFAULT_AUTO_SCROLL_THRESHOLD = 0.8f
    }
    
    /**
     * 渲染模式
     */
    enum class RenderMode {
        /** 逐字符渲染 */
        CHARACTER_BY_CHARACTER,
        /** 逐段落渲染 */
        BLOCK_BY_BLOCK,
        /** 流式追加 */
        STREAMING_APPEND
    }
    
    /**
     * 渲染速度
     */
    enum class RenderSpeed {
        SLOW(100L, 300L),
        NORMAL(50L, 200L),
        FAST(20L, 100L),
        INSTANT(0L, 0L);
        
        val charDelayMs: Long
        val blockDelayMs: Long
        
        constructor(charDelayMs: Long, blockDelayMs: Long) {
            this.charDelayMs = charDelayMs
            this.blockDelayMs = blockDelayMs
        }
    }
    
    /**
     * 渲染配置
     */
    data class Config(
        val mode: RenderMode = RenderMode.BLOCK_BY_BLOCK,
        val speed: RenderSpeed = RenderSpeed.NORMAL,
        val enableAutoScroll: Boolean = true,
        val autoScrollThreshold: Float = DEFAULT_AUTO_SCROLL_THRESHOLD,
        val pauseOnUserScroll: Boolean = true,
        val customCharDelayMs: Long? = null,
        val customBlockDelayMs: Long? = null
    )
    
    /**
     * 渲染状态
     */
    enum class RenderState {
        IDLE, RENDERING, PAUSED, COMPLETED, CANCELLED
    }
    
    /**
     * 渲染监听器
     */
    interface RenderListener {
        fun onRenderStart() {}
        fun onRenderProgress(progress: Float) {}
        fun onRenderComplete() {}
        fun onRenderPaused() {}
        fun onRenderResumed() {}
        fun onRenderCancelled() {}
        fun onRenderError(error: Throwable) {}
    }
    
    private var currentConfig = Config()
    private var currentState = RenderState.IDLE
    private var currentContent = ""
    private var renderedContent = ""
    
    private val handler = Handler(Looper.getMainLooper())
    private val isUserScrolling = AtomicBoolean(false)
    private val isAutoScrolling = AtomicBoolean(false)
    private val currentJob = AtomicInteger(0)
    
    private var renderJob: Job? = null
    private var renderListener: RenderListener? = null
    
    /**
     * 配置渲染器
     */
    fun configure(config: Config): ProgressiveRenderer {
        currentConfig = config
        return this
    }
    
    /**
     * 设置渲染监听器
     */
    fun setRenderListener(listener: RenderListener): ProgressiveRenderer {
        renderListener = listener
        return this
    }
    
    /**
     * 开始渐进式渲染
     */
    fun startRender(markdown: String) {
        if (currentState == RenderState.RENDERING) {
            cancelRender()
        }
        
        currentContent = markdown
        renderedContent = ""
        currentState = RenderState.RENDERING
        currentJob.incrementAndGet()
        
        renderListener?.onRenderStart()
        
        when (currentConfig.mode) {
            RenderMode.CHARACTER_BY_CHARACTER -> startCharacterRender()
            RenderMode.BLOCK_BY_BLOCK -> startBlockRender()
            RenderMode.STREAMING_APPEND -> startStreamingRender()
        }
    }
    
    /**
     * 暂停渲染
     */
    fun pauseRender() {
        if (currentState == RenderState.RENDERING) {
            currentState = RenderState.PAUSED
            renderListener?.onRenderPaused()
        }
    }
    
    /**
     * 恢复渲染
     */
    fun resumeRender() {
        if (currentState == RenderState.PAUSED) {
            currentState = RenderState.RENDERING
            renderListener?.onRenderResumed()
            
            when (currentConfig.mode) {
                RenderMode.CHARACTER_BY_CHARACTER -> resumeCharacterRender()
                RenderMode.BLOCK_BY_BLOCK -> resumeBlockRender()
                RenderMode.STREAMING_APPEND -> resumeStreamingRender()
            }
        }
    }
    
    /**
     * 取消渲染
     */
    fun cancelRender() {
        currentState = RenderState.CANCELLED
        renderJob?.cancel()
        renderJob = null
        renderListener?.onRenderCancelled()
    }
    
    /**
     * 追加内容（流式模式）
     */
    fun appendContent(additionalMarkdown: String) {
        if (currentConfig.mode == RenderMode.STREAMING_APPEND) {
            currentContent += additionalMarkdown
            // 继续流式渲染
            if (currentState == RenderState.RENDERING) {
                continueStreamingRender()
            }
        }
    }
    
    /**
     * 获取当前渲染状态
     */
    fun getRenderState(): RenderState = currentState
    
    /**
     * 获取渲染进度 (0.0 - 1.0)
     */
    fun getRenderProgress(): Float {
        return if (currentContent.isNotEmpty()) {
            (renderedContent.length.toFloat() / currentContent.length).coerceIn(0f, 1f)
        } else 0f
    }
    
    // ========== 私有方法 ==========
    
    private fun startCharacterRender() {
        renderJob = CoroutineScope(Dispatchers.Main).launch {
            val jobId = currentJob.get()
            
            try {
                val lines = currentContent.lines()
                var currentLineIndex = 0
                var currentCharIndex = 0
                
                while (currentLineIndex < lines.size && currentJob.get() == jobId) {
                    val line = lines[currentLineIndex]
                    
                    while (currentCharIndex < line.length && currentJob.get() == jobId) {
                        if (currentState == RenderState.PAUSED) {
                            delay(100)
                            continue
                        }
                        
                        if (currentState == RenderState.CANCELLED) {
                            return@launch
                        }
                        
                        // 添加一个字符
                        val charToAdd = line.substring(0, currentCharIndex + 1)
                        val tempContent = buildString {
                            // 添加已完成的行
                            for (i in 0 until currentLineIndex) {
                                append(lines[i]).append("\n")
                            }
                            // 添加当前行的部分内容
                            append(charToAdd)
                        }
                        
                        renderPartialContent(tempContent)
                        renderedContent = tempContent
                        
                        currentCharIndex++
                        
                        // 延迟
                        val delayMs = currentConfig.customCharDelayMs ?: currentConfig.speed.charDelayMs
                        if (delayMs > 0) {
                            delay(delayMs)
                        }
                        
                        // 检查用户是否手动滚动
                        checkUserScrollAndPause()
                        
                        // 更新进度
                        updateProgress()
                    }
                    
                    // 换行
                    if (currentLineIndex < lines.size - 1) {
                        renderedContent += "\n"
                        renderPartialContent(renderedContent)
                    }
                    
                    currentLineIndex++
                    currentCharIndex = 0
                    
                    // 段落间延迟
                    val blockDelayMs = currentConfig.customBlockDelayMs ?: currentConfig.speed.blockDelayMs
                    if (blockDelayMs > 0) {
                        delay(blockDelayMs)
                    }
                }
                
                if (currentJob.get() == jobId) {
                    completeRender()
                }
                
            } catch (e: Exception) {
                if (currentJob.get() == jobId) {
                    renderListener?.onRenderError(e)
                }
            }
        }
    }
    
    private fun startBlockRender() {
        renderJob = CoroutineScope(Dispatchers.Main).launch {
            val jobId = currentJob.get()
            
            try {
                val blocks = splitIntoBlocks(currentContent)
                
                for (i in blocks.indices) {
                    if (currentJob.get() != jobId) break
                    
                    if (currentState == RenderState.PAUSED) {
                        delay(100)
                        continue
                    }
                    
                    if (currentState == RenderState.CANCELLED) {
                        return@launch
                    }
                    
                    // 渲染当前块
                    val blockToRender = blocks[i]
                    renderedContent += blockToRender
                    renderPartialContent(renderedContent)
                    
                    // 延迟
                    val delayMs = currentConfig.customBlockDelayMs ?: currentConfig.speed.blockDelayMs
                    if (delayMs > 0) {
                        delay(delayMs)
                    }
                    
                    // 检查用户是否手动滚动
                    checkUserScrollAndPause()
                    
                    // 更新进度
                    updateProgress()
                }
                
                if (currentJob.get() == jobId) {
                    completeRender()
                }
                
            } catch (e: Exception) {
                if (currentJob.get() == jobId) {
                    renderListener?.onRenderError(e)
                }
            }
        }
    }
    
    private fun startStreamingRender() {
        renderJob = CoroutineScope(Dispatchers.Main).launch {
            val jobId = currentJob.get()
            
            try {
                val blocks = splitIntoBlocks(currentContent)
                
                for (i in blocks.indices) {
                    if (currentJob.get() != jobId) break
                    
                    if (currentState == RenderState.PAUSED) {
                        delay(100)
                        continue
                    }
                    
                    if (currentState == RenderState.CANCELLED) {
                        return@launch
                    }
                    
                    // 渲染当前块
                    val blockToRender = blocks[i]
                    renderedContent += blockToRender
                    renderPartialContent(renderedContent)
                    
                    // 延迟
                    val delayMs = currentConfig.customBlockDelayMs ?: currentConfig.speed.blockDelayMs
                    if (delayMs > 0) {
                        delay(delayMs)
                    }
                    
                    // 检查用户是否手动滚动
                    checkUserScrollAndPause()
                    
                    // 更新进度
                    updateProgress()
                }
                
                if (currentJob.get() == jobId) {
                    completeRender()
                }
                
            } catch (e: Exception) {
                if (currentJob.get() == jobId) {
                    renderListener?.onRenderError(e)
                }
            }
        }
    }
    
    private fun resumeCharacterRender() {
        // 继续从上次暂停的位置渲染
        startCharacterRender()
    }
    
    private fun resumeBlockRender() {
        // 继续从上次暂停的位置渲染
        startBlockRender()
    }
    
    private fun resumeStreamingRender() {
        // 继续流式渲染
        continueStreamingRender()
    }
    
    private fun continueStreamingRender() {
        // 流式模式的继续渲染逻辑
        if (currentState == RenderState.RENDERING) {
            startStreamingRender()
        }
    }
    
    private fun renderPartialContent(content: String) {
        try {
            val node = markwon.parse(content)
            val spanned = markwon.render(node)
            
            target.post {
                target.text = spanned
                if (currentConfig.enableAutoScroll && !isUserScrolling.get()) {
                    performAutoScroll()
                }
            }
        } catch (e: Exception) {
            renderListener?.onRenderError(e)
        }
    }
    
    private fun splitIntoBlocks(content: String): List<String> {
        val blocks = mutableListOf<String>()
        val lines = content.lines()
        var currentBlock = StringBuilder()
        
        for (line in lines) {
            if (line.trim().isEmpty()) {
                // 空行表示块结束
                if (currentBlock.isNotEmpty()) {
                    blocks.add(currentBlock.toString())
                    currentBlock.clear()
                }
                blocks.add("\n")
            } else if (line.startsWith("#") || line.startsWith(">") || line.startsWith("-") || line.startsWith("*") || line.startsWith("`")) {
                // 特殊语法行，单独作为块
                if (currentBlock.isNotEmpty()) {
                    blocks.add(currentBlock.toString())
                    currentBlock.clear()
                }
                blocks.add(line + "\n")
            } else {
                currentBlock.append(line).append("\n")
            }
        }
        
        // 添加最后一个块
        if (currentBlock.isNotEmpty()) {
            blocks.add(currentBlock.toString())
        }
        
        return blocks
    }
    
    private fun checkUserScrollAndPause() {
        if (currentConfig.pauseOnUserScroll && isUserScrolling.get()) {
            pauseRender()
        }
    }
    
    private fun performAutoScroll() {
        if (!currentConfig.enableAutoScroll) return
        
        val scrollView = this.scrollView ?: this.nestedScrollView
        scrollView?.let { sv ->
            val scrollable = sv.getChildAt(0)
            val scrollableHeight = scrollable.height
            val scrollViewHeight = sv.height
            val currentScrollY = sv.scrollY
            
            val scrollThreshold = (scrollableHeight - scrollViewHeight) * currentConfig.autoScrollThreshold
            
            if (currentScrollY < scrollThreshold) {
                isAutoScrolling.set(true)
                sv.post {
                    sv.smoothScrollTo(0, scrollableHeight)
                    isAutoScrolling.set(false)
                }
            }
        }
    }
    
    private fun updateProgress() {
        val progress = getRenderProgress()
        renderListener?.onRenderProgress(progress)
    }
    
    private fun completeRender() {
        currentState = RenderState.COMPLETED
        renderListener?.onRenderComplete()
    }
    
    /**
     * 设置滚动监听器（需要在外部调用）
     */
    fun setupScrollListener() {
        scrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY != oldScrollY && !isAutoScrolling.get()) {
                isUserScrolling.set(true)
                handler.postDelayed({
                    isUserScrolling.set(false)
                }, 1000) // 1秒后重置滚动状态
            }
        }
        
        nestedScrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY != oldScrollY && !isAutoScrolling.get()) {
                isUserScrolling.set(true)
                handler.postDelayed({
                    isUserScrolling.set(false)
                }, 1000) // 1秒后重置滚动状态
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        cancelRender()
        renderListener = null
    }
}