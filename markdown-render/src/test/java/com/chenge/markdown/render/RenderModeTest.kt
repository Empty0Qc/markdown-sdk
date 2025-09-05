package com.chenge.markdown.render

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 渲染模式测试
 */
class RenderModeTest {
    
    private val testMarkdown = "# 测试标题\n\n这是测试内容。"
    
    @Before
    fun setup() {
        // 测试初始化
    }
    
    @Test
    fun testRenderModeEnum() {
        // 测试渲染模式枚举
        assertEquals(3, RenderMode.values().size)
        assertTrue(RenderMode.values().contains(RenderMode.SYNC))
        assertTrue(RenderMode.values().contains(RenderMode.ASYNC))
        assertTrue(RenderMode.values().contains(RenderMode.COROUTINE))
    }
    
    @Test
    fun testRenderConfig() {
        // 测试默认配置
        val defaultConfig = RenderConfig()
        assertEquals(RenderMode.ASYNC, defaultConfig.mode)
        assertEquals(5000L, defaultConfig.timeoutMs)
        assertTrue(defaultConfig.enableCache)
        assertEquals(ErrorHandling.FALLBACK_TO_SYNC, defaultConfig.errorHandling)
        assertFalse(defaultConfig.enablePerformanceMonitoring)
        assertEquals(3, defaultConfig.maxConcurrentTasks)
        
        // 测试自定义配置
        val customConfig = RenderConfig(
            mode = RenderMode.SYNC,
            timeoutMs = 3000L,
            enableCache = false,
            errorHandling = ErrorHandling.SHOW_ERROR,
            enablePerformanceMonitoring = true,
            maxConcurrentTasks = 5
        )
        assertEquals(RenderMode.SYNC, customConfig.mode)
        assertEquals(3000L, customConfig.timeoutMs)
        assertFalse(customConfig.enableCache)
        assertEquals(ErrorHandling.SHOW_ERROR, customConfig.errorHandling)
        assertTrue(customConfig.enablePerformanceMonitoring)
        assertEquals(5, customConfig.maxConcurrentTasks)
    }
    
    @Test
    fun testErrorHandlingEnum() {
        // 测试错误处理枚举
        assertEquals(4, ErrorHandling.values().size)
        assertTrue(ErrorHandling.values().contains(ErrorHandling.FALLBACK_TO_SYNC))
        assertTrue(ErrorHandling.values().contains(ErrorHandling.SHOW_ERROR))
        assertTrue(ErrorHandling.values().contains(ErrorHandling.SILENT_FAIL))
        assertTrue(ErrorHandling.values().contains(ErrorHandling.THROW_EXCEPTION))
    }
    
    @Test
    fun testRenderResult() {
        // 测试成功结果
        val successResult = RenderResult.Success(
            content = "测试内容",
            renderTimeMs = 100L,
            mode = RenderMode.SYNC
        )
        assertTrue(successResult is RenderResult.Success)
        assertEquals("测试内容", successResult.content)
        assertEquals(100L, successResult.renderTimeMs)
        assertEquals(RenderMode.SYNC, successResult.mode)
        
        // 测试错误结果
        val errorResult = RenderResult.Error(
            exception = RuntimeException("测试错误"),
            mode = RenderMode.ASYNC,
            fallbackContent = "备用内容"
        )
        assertTrue(errorResult is RenderResult.Error)
        assertEquals("测试错误", errorResult.exception.message)
        assertEquals(RenderMode.ASYNC, errorResult.mode)
        assertEquals("备用内容", errorResult.fallbackContent)
        
        // 测试取消结果
        val cancelledResult = RenderResult.Cancelled(
            mode = RenderMode.COROUTINE,
            reason = "用户取消"
        )
        assertTrue(cancelledResult is RenderResult.Cancelled)
        assertEquals(RenderMode.COROUTINE, cancelledResult.mode)
        assertEquals("用户取消", cancelledResult.reason)
    }
    
    @Test
    fun testRenderPerformance() {
        // 测试性能数据
        val performance = RenderPerformance(
            parseTimeMs = 50L,
            renderTimeMs = 100L,
            totalTimeMs = 150L,
            mode = RenderMode.ASYNC,
            contentLength = 1000,
            cacheHit = true
        )
        assertEquals(50L, performance.parseTimeMs)
        assertEquals(100L, performance.renderTimeMs)
        assertEquals(150L, performance.totalTimeMs)
        assertEquals(RenderMode.ASYNC, performance.mode)
        assertEquals(1000, performance.contentLength)
        assertTrue(performance.cacheHit)
    }
    
    @Test
    fun testEnhancedMarkdownRenderer() {
        // 测试渲染器创建
        val renderer = EnhancedMarkdownRenderer()
        assertNotNull(renderer)
        
        // 测试缓存统计
        val cacheStats = renderer.getCacheStats()
        assertNotNull(cacheStats)
        assertEquals(0, cacheStats.size)
        assertTrue(cacheStats.enabled)
        
        // 测试活动任务统计
        val taskStats = renderer.getActiveTaskStats()
        assertNotNull(taskStats)
        assertEquals(0, taskStats.coroutineJobs)
        assertEquals(0, taskStats.asyncFutures)
    }
    
    @Test
    fun testRendererCacheOperations() {
        val renderer = EnhancedMarkdownRenderer()
        
        // 测试清除缓存
        renderer.clearCache()
        val statsAfterClear = renderer.getCacheStats()
        assertEquals(0, statsAfterClear.size)
        
        // 测试取消所有任务
        renderer.cancelAll()
        val taskStatsAfterCancel = renderer.getActiveTaskStats()
        assertEquals(0, taskStatsAfterCancel.coroutineJobs)
        assertEquals(0, taskStatsAfterCancel.asyncFutures)
    }
    
    @Test
    fun testRenderListener() {
        var callbackResult: RenderResult? = null
        val listener = object : RenderListener {
            override fun onRenderComplete(result: RenderResult) {
                callbackResult = result
            }
        }
        
        // 模拟回调
        val testResult = RenderResult.Success(
            content = "测试",
            renderTimeMs = 50L,
            mode = RenderMode.SYNC
        )
        listener.onRenderComplete(testResult)
        
        assertNotNull(callbackResult)
        assertTrue(callbackResult is RenderResult.Success)
        assertEquals("测试", (callbackResult as RenderResult.Success).content)
    }
}