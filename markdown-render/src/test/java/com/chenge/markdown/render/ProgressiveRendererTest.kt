package com.chenge.markdown.render

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 渐进式渲染器测试
 */
class ProgressiveRendererTest {
    
    private lateinit var progressiveRenderer: ProgressiveRenderer
    
    @Before
    fun setup() {
        progressiveRenderer = ProgressiveRenderer()
    }
    
    @Test
    fun testProgressiveModeEnum() {
        // 测试渐进式渲染模式枚举
        val modes = ProgressiveMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(ProgressiveMode.CHARACTER_BY_CHARACTER))
        assertTrue(modes.contains(ProgressiveMode.PARAGRAPH_BY_PARAGRAPH))
        assertTrue(modes.contains(ProgressiveMode.STREAMING))
    }
    
    @Test
    fun testProgressiveConfig() {
        // 测试默认配置
        val defaultConfig = ProgressiveConfig()
        assertEquals(ProgressiveMode.PARAGRAPH_BY_PARAGRAPH, defaultConfig.mode)
        assertEquals(50L, defaultConfig.intervalMs)
        assertTrue(defaultConfig.enableAnimation)
        assertEquals(1, defaultConfig.maxConcurrentRenders)
        assertFalse(defaultConfig.autoScroll)
        assertEquals(1024, defaultConfig.streamBufferSize)
        
        // 测试自定义配置
        val customConfig = ProgressiveConfig(
            mode = ProgressiveMode.CHARACTER_BY_CHARACTER,
            intervalMs = 100L,
            enableAnimation = false,
            maxConcurrentRenders = 2,
            autoScroll = true,
            streamBufferSize = 512
        )
        assertEquals(ProgressiveMode.CHARACTER_BY_CHARACTER, customConfig.mode)
        assertEquals(100L, customConfig.intervalMs)
        assertFalse(customConfig.enableAnimation)
        assertEquals(2, customConfig.maxConcurrentRenders)
        assertTrue(customConfig.autoScroll)
        assertEquals(512, customConfig.streamBufferSize)
    }
    
    @Test
    fun testProgressiveState() {
        // 测试渐进式渲染状态
        val idleState = ProgressiveState.Idle
        assertTrue(idleState is ProgressiveState.Idle)
        
        val renderingState = ProgressiveState.Rendering(0.5f, "test content")
        assertTrue(renderingState is ProgressiveState.Rendering)
        assertEquals(0.5f, renderingState.progress, 0.001f)
        assertEquals("test content", renderingState.currentContent)
        
        val completedState = ProgressiveState.Completed("final content", 1000L)
        assertTrue(completedState is ProgressiveState.Completed)
        assertEquals("final content", completedState.finalContent)
        assertEquals(1000L, completedState.totalTimeMs)
        
        val errorState = ProgressiveState.Error(RuntimeException("test error"))
        assertTrue(errorState is ProgressiveState.Error)
        assertEquals("test error", errorState.exception.message)
        
        val cancelledState = ProgressiveState.Cancelled("test reason")
        assertTrue(cancelledState is ProgressiveState.Cancelled)
        assertEquals("test reason", cancelledState.reason)
    }
    
    @Test
    fun testProgressiveRendererCreation() {
        // 测试渐进式渲染器创建
        val renderer1 = ProgressiveRenderer()
        assertNotNull(renderer1)
        assertFalse(renderer1.isRendering())
        
        val config = ProgressiveConfig(mode = ProgressiveMode.STREAMING)
        val renderer2 = ProgressiveRenderer(config)
        assertNotNull(renderer2)
        assertFalse(renderer2.isRendering())
    }
    
    @Test
    fun testProgressiveRendererStats() {
        // 测试渲染统计信息
        val stats = progressiveRenderer.getRenderStats()
        assertNotNull(stats)
        assertTrue(stats.instanceId > 0)
        assertFalse(stats.isRendering)
        assertEquals(ProgressiveMode.PARAGRAPH_BY_PARAGRAPH, stats.currentMode)
        assertEquals(50L, stats.intervalMs)
    }
    
    @Test
    fun testProgressiveRendererStopWhenNotRendering() {
        // 测试在未渲染时停止渲染
        assertFalse(progressiveRenderer.isRendering())
        progressiveRenderer.stopProgressiveRender()
        assertFalse(progressiveRenderer.isRendering())
    }
    
    @Test
    fun testProgressiveRenderListener() {
        // 测试渐进式渲染监听器
        var stateChangedCalled = false
        var progressUpdateCalled = false
        var renderCompleteCalled = false
        var renderErrorCalled = false
        
        val listener = object : ProgressiveRenderListener {
            override fun onStateChanged(state: ProgressiveState) {
                stateChangedCalled = true
            }
            
            override fun onProgressUpdate(progress: Float, currentContent: String) {
                progressUpdateCalled = true
            }
            
            override fun onRenderComplete(finalContent: CharSequence, totalTimeMs: Long) {
                renderCompleteCalled = true
            }
            
            override fun onRenderError(exception: Throwable) {
                renderErrorCalled = true
            }
        }
        
        // 测试监听器方法调用
        listener.onStateChanged(ProgressiveState.Idle)
        assertTrue(stateChangedCalled)
        
        listener.onProgressUpdate(0.5f, "test")
        assertTrue(progressUpdateCalled)
        
        listener.onRenderComplete("final", 1000L)
        assertTrue(renderCompleteCalled)
        
        listener.onRenderError(RuntimeException("test"))
        assertTrue(renderErrorCalled)
    }
    
    @Test
    fun testProgressiveConfigModes() {
        // 测试不同渐进式渲染模式的配置
        val characterConfig = ProgressiveConfig(mode = ProgressiveMode.CHARACTER_BY_CHARACTER)
        val characterRenderer = ProgressiveRenderer(characterConfig)
        assertEquals(ProgressiveMode.CHARACTER_BY_CHARACTER, characterRenderer.getRenderStats().currentMode)
        
        val paragraphConfig = ProgressiveConfig(mode = ProgressiveMode.PARAGRAPH_BY_PARAGRAPH)
        val paragraphRenderer = ProgressiveRenderer(paragraphConfig)
        assertEquals(ProgressiveMode.PARAGRAPH_BY_PARAGRAPH, paragraphRenderer.getRenderStats().currentMode)
        
        val streamingConfig = ProgressiveConfig(mode = ProgressiveMode.STREAMING)
        val streamingRenderer = ProgressiveRenderer(streamingConfig)
        assertEquals(ProgressiveMode.STREAMING, streamingRenderer.getRenderStats().currentMode)
    }
    
    @Test
    fun testProgressiveStatsDataClass() {
        // 测试渐进式渲染统计信息数据类
        val stats1 = ProgressiveRenderer.ProgressiveStats(
            instanceId = 1,
            isRendering = false,
            currentMode = ProgressiveMode.CHARACTER_BY_CHARACTER,
            intervalMs = 50L
        )
        
        val stats2 = ProgressiveRenderer.ProgressiveStats(
            instanceId = 1,
            isRendering = false,
            currentMode = ProgressiveMode.CHARACTER_BY_CHARACTER,
            intervalMs = 50L
        )
        
        assertEquals(stats1, stats2)
        assertEquals(stats1.hashCode(), stats2.hashCode())
        assertEquals(stats1.toString(), stats2.toString())
    }
    
    @Test
    fun testMultipleProgressiveRendererInstances() {
        // 测试多个渐进式渲染器实例
        val renderer1 = ProgressiveRenderer()
        val renderer2 = ProgressiveRenderer()
        val renderer3 = ProgressiveRenderer()
        
        val stats1 = renderer1.getRenderStats()
        val stats2 = renderer2.getRenderStats()
        val stats3 = renderer3.getRenderStats()
        
        // 每个实例应该有不同的ID
        assertNotEquals(stats1.instanceId, stats2.instanceId)
        assertNotEquals(stats2.instanceId, stats3.instanceId)
        assertNotEquals(stats1.instanceId, stats3.instanceId)
        
        // 但其他属性应该相同（使用默认配置）
        assertEquals(stats1.currentMode, stats2.currentMode)
        assertEquals(stats1.intervalMs, stats2.intervalMs)
        assertEquals(stats1.isRendering, stats2.isRendering)
    }
}