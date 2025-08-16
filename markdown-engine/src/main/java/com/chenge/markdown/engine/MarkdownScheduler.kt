package com.chenge.markdown.engine

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Markdown 渲染专用调度器
 * 提供独立的线程池用于 Markdown 解析和渲染，避免阻塞主线程
 */
object MarkdownScheduler {
    
    private const val CORE_POOL_SIZE = 2
    private const val MAX_POOL_SIZE = 4
    
    /**
     * 专用于 Markdown 渲染的线程池
     */
    private val renderExecutor: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            CORE_POOL_SIZE,
            MarkdownThreadFactory("MarkdownRender")
        )
    }
    
    /**
     * 专用于图片加载的线程池
     */
    private val imageExecutor: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            MAX_POOL_SIZE,
            MarkdownThreadFactory("MarkdownImage")
        )
    }
    
    /**
     * 主线程 Handler
     */
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 在渲染线程池中执行任务
     */
    fun executeRender(task: Runnable): Future<*> {
        return renderExecutor.submit(task)
    }
    
    /**
     * 在图片加载线程池中执行任务
     */
    fun executeImage(task: Runnable): Future<*> {
        return imageExecutor.submit(task)
    }
    
    /**
     * 在主线程中执行任务
     */
    fun executeOnMain(task: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run()
        } else {
            mainHandler.post(task)
        }
    }
    
    /**
     * 延迟在主线程中执行任务
     */
    fun executeOnMainDelayed(task: Runnable, delayMillis: Long) {
        mainHandler.postDelayed(task, delayMillis)
    }
    
    /**
     * 异步执行渲染任务并在主线程回调结果
     */
    fun <T> asyncRender(
        backgroundTask: () -> T,
        onResult: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ): Future<*> {
        return executeRender {
            try {
                val result = backgroundTask()
                executeOnMain {
                    onResult(result)
                }
            } catch (e: Throwable) {
                executeOnMain {
                    onError?.invoke(e) ?: throw e
                }
            }
        }
    }
    
    /**
     * 检查当前是否在主线程
     */
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
    
    /**
     * 获取调度器状态
     */
    fun getSchedulerStats(): SchedulerStats {
        return SchedulerStats(
            renderPoolActive = !renderExecutor.isShutdown,
            imagePoolActive = !imageExecutor.isShutdown,
            isMainThread = isMainThread()
        )
    }
    
    /**
     * 关闭调度器（通常在应用退出时调用）
     */
    fun shutdown() {
        renderExecutor.shutdown()
        imageExecutor.shutdown()
    }
    
    /**
     * 立即关闭调度器
     */
    fun shutdownNow() {
        renderExecutor.shutdownNow()
        imageExecutor.shutdownNow()
    }
    
    /**
     * 自定义线程工厂
     */
    private class MarkdownThreadFactory(private val namePrefix: String) : ThreadFactory {
        private val threadNumber = AtomicInteger(1)
        
        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r, "$namePrefix-${threadNumber.getAndIncrement()}")
            thread.isDaemon = false
            thread.priority = Thread.NORM_PRIORITY
            return thread
        }
    }
    
    data class SchedulerStats(
        val renderPoolActive: Boolean,
        val imagePoolActive: Boolean,
        val isMainThread: Boolean
    )
}