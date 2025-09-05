package com.chenge.markdown.render

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Markdown 渲染模式
 */
enum class RenderMode {
    /**
     * 同步渲染模式
     * - 在当前线程直接执行渲染
     * - 适用于小文档或需要立即显示的场景
     * - 可能阻塞UI线程，需谨慎使用
     */
    SYNC,
    
    /**
     * 异步渲染模式
     * - 在后台线程执行解析和渲染
     * - 结果回调到主线程更新UI
     * - 适用于大文档或复杂内容
     * - 推荐的默认模式
     */
    ASYNC,
    
    /**
     * 协程渲染模式
     * - 使用Kotlin协程进行异步渲染
     * - 支持取消和超时控制
     * - 更现代的异步处理方式
     */
    COROUTINE
}

/**
 * 渲染配置
 */
data class RenderConfig(
    /**
     * 渲染模式
     */
    val mode: RenderMode = RenderMode.ASYNC,
    
    /**
     * 渲染超时时间（毫秒）
     * 仅在异步模式下生效
     */
    val timeoutMs: Long = 5000L,
    
    /**
     * 是否启用渲染缓存
     */
    val enableCache: Boolean = true,
    
    /**
     * 协程调度器（仅在协程模式下使用）
     */
    val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    
    /**
     * 错误处理策略
     */
    val errorHandling: ErrorHandling = ErrorHandling.FALLBACK_TO_SYNC,
    
    /**
     * 是否启用性能监控
     */
    val enablePerformanceMonitoring: Boolean = false,
    
    /**
     * 最大并发渲染任务数
     */
    val maxConcurrentTasks: Int = 3
)

/**
 * 错误处理策略
 */
enum class ErrorHandling {
    /**
     * 降级到同步渲染
     */
    FALLBACK_TO_SYNC,
    
    /**
     * 显示错误信息
     */
    SHOW_ERROR,
    
    /**
     * 静默失败（显示空内容）
     */
    SILENT_FAIL,
    
    /**
     * 抛出异常
     */
    THROW_EXCEPTION
}

/**
 * 渲染结果
 */
sealed class RenderResult {
    /**
     * 渲染成功
     */
    data class Success(
        val content: CharSequence,
        val renderTimeMs: Long,
        val mode: RenderMode
    ) : RenderResult()
    
    /**
     * 渲染失败
     */
    data class Error(
        val exception: Throwable,
        val mode: RenderMode,
        val fallbackContent: CharSequence? = null
    ) : RenderResult()
    
    /**
     * 渲染取消
     */
    data class Cancelled(
        val mode: RenderMode,
        val reason: String
    ) : RenderResult()
}

/**
 * 渲染监听器
 */
interface RenderListener {
    /**
     * 渲染开始
     */
    fun onRenderStart(mode: RenderMode) {}
    
    /**
     * 渲染完成
     */
    fun onRenderComplete(result: RenderResult) {}
    
    /**
     * 渲染进度更新（仅在支持的模式下）
     */
    fun onRenderProgress(progress: Float) {}
}

/**
 * 渲染性能统计
 */
data class RenderPerformance(
    val parseTimeMs: Long,
    val renderTimeMs: Long,
    val totalTimeMs: Long,
    val mode: RenderMode,
    val contentLength: Int,
    val cacheHit: Boolean = false
)