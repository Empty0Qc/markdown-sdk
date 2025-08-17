package com.chenge.markdown.plugins

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.image.AsyncDrawable
// import io.noties.markwon.image.ImageSpan // 暂时注释掉
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 * 性能优化插件
 * 提供图片懒加载、渲染缓存、内存管理等性能优化功能
 */
class PerformancePlugin(
    private val config: MarkdownStyleConfigV2,
    private val cacheSize: Int = 50 * 1024 * 1024 // 50MB
) : AbstractMarkwonPlugin(), LifecycleObserver {
    
    private val renderCache = RenderCache(cacheSize)
    private val imageCache = ImageCache(cacheSize / 4) // 图片缓存占总缓存的1/4
    private val lazyImageLoader = LazyImageLoader(imageCache)
    private val memoryManager = MemoryManager()
    private val performanceMonitor = PerformanceMonitor()
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configurePerformanceSpans(builder)
    }
    
    private fun configurePerformanceSpans(builder: MarkwonSpansFactory.Builder) {
        // 配置懒加载图片 - 暂时注释掉，使用默认图片处理
        // builder.setFactory(org.commonmark.node.Image::class.java) { _, props ->
        //     val url = props.get("destination") as? String ?: ""
        //     val alt = props.get("title") as? String ?: ""
        //     
        //     arrayOf(
        //         LazyImageSpan(lazyImageLoader, url, alt, config)
        //     )
        // }
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cleanup()
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        renderCache.clear()
        imageCache.clear()
        lazyImageLoader.cleanup()
        memoryManager.cleanup()
    }
    
    /**
     * 获取性能统计信息
     */
    fun getPerformanceStats(): PerformanceStats {
        return performanceMonitor.getStats()
    }
    
    /**
     * 预渲染Markdown内容
     */
    fun preRender(markwon: Markwon, markdown: String): String {
        val cacheKey = generateCacheKey(markdown)
        
        return renderCache.get(cacheKey) ?: run {
            val startTime = System.currentTimeMillis()
            
            val rendered = markwon.toMarkdown(markdown).toString()
            renderCache.put(cacheKey, rendered)
            
            val renderTime = System.currentTimeMillis() - startTime
            performanceMonitor.recordRenderTime(renderTime)
            
            rendered
        }
    }
    
    private fun generateCacheKey(content: String): String {
        return content.hashCode().toString()
    }
}

/**
 * 渲染缓存
 */
class RenderCache(maxSize: Int) {
    private val cache = LruCache<String, String>(maxSize / 1024) // 按KB计算
    private val accessTimes = ConcurrentHashMap<String, Long>()
    
    fun get(key: String): String? {
        accessTimes[key] = System.currentTimeMillis()
        return cache.get(key)
    }
    
    fun put(key: String, value: String) {
        accessTimes[key] = System.currentTimeMillis()
        cache.put(key, value)
    }
    
    fun clear() {
        cache.evictAll()
        accessTimes.clear()
    }
    
    fun size(): Int = cache.size()
    
    fun hitCount(): Int = cache.hitCount()
    
    fun missCount(): Int = cache.missCount()
    
    /**
     * 清理过期缓存
     */
    fun cleanupExpired(maxAge: Long = 30 * 60 * 1000) { // 30分钟
        val currentTime = System.currentTimeMillis()
        val expiredKeys = accessTimes.entries
            .filter { currentTime - it.value > maxAge }
            .map { it.key }
        
        expiredKeys.forEach { key ->
            cache.remove(key)
            accessTimes.remove(key)
        }
    }
}

/**
 * 图片缓存
 */
class ImageCache(maxSize: Int) {
    private val bitmapCache = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
        
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }
    }
    
    private val drawableCache = LruCache<String, Drawable>(100)
    
    fun getBitmap(key: String): Bitmap? = bitmapCache.get(key)
    
    fun putBitmap(key: String, bitmap: Bitmap) {
        bitmapCache.put(key, bitmap)
    }
    
    fun getDrawable(key: String): Drawable? = drawableCache.get(key)
    
    fun putDrawable(key: String, drawable: Drawable) {
        drawableCache.put(key, drawable)
    }
    
    fun clear() {
        bitmapCache.evictAll()
        drawableCache.evictAll()
    }
    
    fun size(): Int = bitmapCache.size() + drawableCache.size()
}

/**
 * 懒加载图片加载器
 */
class LazyImageLoader(private val imageCache: ImageCache) {
    
    private val loadingTasks = ConcurrentHashMap<String, Job>()
    private val threadPool = Executors.newFixedThreadPool(4) as ThreadPoolExecutor
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 懒加载图片
     */
    fun loadImage(
        url: String,
        callback: (Drawable?) -> Unit
    ) {
        // 先检查缓存
        imageCache.getDrawable(url)?.let { cached ->
            callback(cached)
            return
        }
        
        // 避免重复加载
        if (loadingTasks.containsKey(url)) {
            return
        }
        
        // 异步加载
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val drawable = loadImageFromUrl(url)
                
                withContext(Dispatchers.Main) {
                    drawable?.let { imageCache.putDrawable(url, it) }
                    callback(drawable)
                    loadingTasks.remove(url)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null)
                    loadingTasks.remove(url)
                }
            }
        }
        
        loadingTasks[url] = job
    }
    
    private suspend fun loadImageFromUrl(url: String): Drawable? {
        // 这里应该实现实际的图片加载逻辑
        // 可以使用 Glide、Picasso 或其他图片加载库
        return withContext(Dispatchers.IO) {
            try {
                // 模拟图片加载
                delay(100) // 模拟网络延迟
                
                // 实际实现中应该从URL加载图片
                // val inputStream = URL(url).openStream()
                // val bitmap = BitmapFactory.decodeStream(inputStream)
                // BitmapDrawable(Resources.getSystem(), bitmap)
                
                null // 占位符
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * 预加载图片
     */
    fun preloadImages(urls: List<String>) {
        urls.forEach { url ->
            if (!imageCache.getDrawable(url).let { it != null }) {
                loadImage(url) { /* 预加载，不需要回调 */ }
            }
        }
    }
    
    /**
     * 取消加载任务
     */
    fun cancelLoad(url: String) {
        loadingTasks[url]?.cancel()
        loadingTasks.remove(url)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        loadingTasks.values.forEach { it.cancel() }
        loadingTasks.clear()
        threadPool.shutdown()
    }
}

/**
 * 懒加载图片Span
 */
class LazyImageSpan(
    private val lazyImageLoader: LazyImageLoader,
    private val url: String,
    private val alt: String?,
    private val config: MarkdownStyleConfigV2
) : android.text.style.ImageSpan(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)) {
    
    private var textViewRef: WeakReference<TextView>? = null
    private var isLoaded = false
    
    override fun draw(
        canvas: android.graphics.Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: android.graphics.Paint
    ) {
        if (!isLoaded) {
            // 显示占位符
            drawPlaceholder(canvas, x, top, bottom, paint)
            
            // 开始加载图片
            loadImageAsync()
        } else {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint)
        }
    }
    
    private fun drawPlaceholder(
        canvas: android.graphics.Canvas,
        x: Float,
        top: Int,
        bottom: Int,
        paint: android.graphics.Paint
    ) {
        val placeholderPaint = android.graphics.Paint().apply {
            color = config.surfaceColor
            style = android.graphics.Paint.Style.FILL
        }
        
        val textPaint = android.graphics.Paint().apply {
            color = config.onSurfaceColor
            textSize = config.textSize * 0.8f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val width = 200f
        val height = (bottom - top).toFloat()
        
        // 绘制占位符背景
        canvas.drawRect(x, top.toFloat(), x + width, bottom.toFloat(), placeholderPaint)
        
        // 绘制加载文本
        val centerX = x + width / 2
        val centerY = top + height / 2 + textPaint.textSize / 3
        canvas.drawText("📷 加载中...", centerX, centerY, textPaint)
    }
    
    private fun loadImageAsync() {
        if (isLoaded) return
        
        lazyImageLoader.loadImage(url) { drawable ->
            drawable?.let {
                // 更新图片
                isLoaded = true
                
                // 通知TextView重新绘制
                textViewRef?.get()?.invalidate()
            }
        }
    }
    
    /**
     * 设置关联的TextView
     */
    fun setTextView(textView: TextView) {
        textViewRef = WeakReference(textView)
    }
}

/**
 * 内存管理器
 */
class MemoryManager {
    
    private val memoryThreshold = 0.8f // 内存使用率阈值
    private val gcHandler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    
    /**
     * 开始内存监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        val runnable = object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    checkMemoryUsage()
                    gcHandler.postDelayed(this, 5000) // 每5秒检查一次
                }
            }
        }
        
        gcHandler.post(runnable)
    }
    
    /**
     * 停止内存监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        gcHandler.removeCallbacksAndMessages(null)
    }
    
    /**
     * 检查内存使用情况
     */
    private fun checkMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsage = usedMemory.toFloat() / maxMemory.toFloat()
        
        if (memoryUsage > memoryThreshold) {
            // 内存使用率过高，执行清理
            performMemoryCleanup()
        }
    }
    
    /**
     * 执行内存清理
     */
    private fun performMemoryCleanup() {
        // 建议垃圾回收
        System.gc()
        
        // 可以在这里添加更多清理逻辑
        // 比如清理缓存、释放不必要的资源等
    }
    
    /**
     * 获取内存使用信息
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()
        
        return MemoryInfo(
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            maxMemory = maxMemory,
            usagePercentage = (usedMemory.toFloat() / maxMemory.toFloat() * 100).toInt()
        )
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        stopMonitoring()
    }
}

/**
 * 性能监控器
 */
class PerformanceMonitor {
    
    private val renderTimes = mutableListOf<Long>()
    private val maxRecords = 100
    
    /**
     * 记录渲染时间
     */
    fun recordRenderTime(timeMs: Long) {
        synchronized(renderTimes) {
            renderTimes.add(timeMs)
            if (renderTimes.size > maxRecords) {
                renderTimes.removeAt(0)
            }
        }
    }
    
    /**
     * 获取性能统计信息
     */
    fun getStats(): PerformanceStats {
        synchronized(renderTimes) {
            if (renderTimes.isEmpty()) {
                return PerformanceStats()
            }
            
            val avgRenderTime = renderTimes.average()
            val maxRenderTime = renderTimes.maxOrNull() ?: 0L
            val minRenderTime = renderTimes.minOrNull() ?: 0L
            
            return PerformanceStats(
                averageRenderTime = avgRenderTime,
                maxRenderTime = maxRenderTime,
                minRenderTime = minRenderTime,
                totalRenders = renderTimes.size
            )
        }
    }
    
    /**
     * 清理统计数据
     */
    fun clear() {
        synchronized(renderTimes) {
            renderTimes.clear()
        }
    }
}

/**
 * 内存信息数据类
 */
data class MemoryInfo(
    val usedMemory: Long,
    val freeMemory: Long,
    val maxMemory: Long,
    val usagePercentage: Int
)

/**
 * 性能统计数据类
 */
data class PerformanceStats(
    val averageRenderTime: Double = 0.0,
    val maxRenderTime: Long = 0L,
    val minRenderTime: Long = 0L,
    val totalRenders: Int = 0
)

/**
 * 分页渲染器
 * 用于处理大文档的分页渲染
 */
class PaginatedRenderer(
    private val pageSize: Int = 1000, // 每页字符数
    private val config: MarkdownStyleConfigV2
) {
    
    /**
     * 将大文档分页
     */
    fun paginate(content: String): List<String> {
        val pages = mutableListOf<String>()
        var currentIndex = 0
        
        while (currentIndex < content.length) {
            val endIndex = minOf(currentIndex + pageSize, content.length)
            
            // 尝试在合适的位置分页（避免在单词中间分割）
            val actualEndIndex = findSuitableBreakPoint(content, currentIndex, endIndex)
            
            pages.add(content.substring(currentIndex, actualEndIndex))
            currentIndex = actualEndIndex
        }
        
        return pages
    }
    
    private fun findSuitableBreakPoint(content: String, start: Int, end: Int): Int {
        if (end >= content.length) return end
        
        // 寻找最近的换行符或空格
        for (i in end downTo start + pageSize / 2) {
            if (content[i] == '\n' || content[i] == ' ') {
                return i + 1
            }
        }
        
        return end
    }
}

/**
 * 性能优化工具类
 */
object PerformanceUtils {
    
    /**
     * 测量渲染性能
     */
    fun measureRenderTime(action: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        action()
        return System.currentTimeMillis() - startTime
    }
    
    /**
     * 检查是否需要分页渲染
     */
    fun shouldUsePagination(content: String, threshold: Int = 10000): Boolean {
        return content.length > threshold
    }
    
    /**
     * 优化TextView性能
     */
    fun optimizeTextView(textView: TextView) {
        // 禁用硬件加速（在某些情况下可能更快）
        textView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        
        // 设置文本缓存
        textView.setTextIsSelectable(false) // 如果不需要选择功能
        
        // 优化滚动性能
        if (textView.parent is android.widget.ScrollView) {
            (textView.parent as android.widget.ScrollView).isSmoothScrollingEnabled = true
        }
    }
    
    /**
     * 预热渲染器
     */
    fun warmupRenderer(markwon: Markwon) {
        // 使用小的测试内容预热渲染器
        val testContent = "# Test\n\nThis is a test."
        markwon.toMarkdown(testContent)
    }
}