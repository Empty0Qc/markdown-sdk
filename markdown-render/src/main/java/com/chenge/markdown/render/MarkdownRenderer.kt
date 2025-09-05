package com.chenge.markdown.render

import android.util.Log
import android.widget.TextView
import com.chenge.markdown.render.MarkdownScheduler
import io.noties.markwon.Markwon

/**
 * MarkdownRenderer：负责渲染Markdown
 * 提供向后兼容的API，同时支持新的渲染模式
 */
object MarkdownRenderer {

  private val defaultEnhancedRenderer = EnhancedMarkdownRenderer()
  
  /**
   * 同步渲染Markdown（生产可用）
   */
  fun setMarkdownSync(markwon: Markwon, textView: TextView, markdown: String) {
    markwon.setMarkdown(textView, markdown)
  }

  /**
   * 异步渲染Markdown（生产可用）- 使用专用调度器优化性能
   */
  fun setMarkdownAsync(markwon: Markwon, textView: TextView, markdown: String) {
    MarkdownScheduler.asyncRender(
      backgroundTask = {
        val node = markwon.parse(markdown)
        markwon.render(node)
      },
      onResult = { spanned ->
        markwon.setParsedMarkdown(textView, spanned)
      },
      onError = { error ->
        Log.e("MarkdownRenderer", "异步渲染失败", error)
        // 降级到同步渲染
        setMarkdownSync(markwon, textView, markdown)
      }
    )
  }
  
  /**
   * 使用指定配置渲染Markdown
   */
  fun render(
    markwon: Markwon,
    textView: TextView,
    markdown: String,
    config: RenderConfig = RenderConfig(),
    listener: RenderListener? = null
  ) {
    val renderer = EnhancedMarkdownRenderer(config)
    renderer.render(markwon, textView, markdown, listener)
  }
  
  /**
   * 使用默认配置的增强渲染器
   */
  fun renderEnhanced(
    markwon: Markwon,
    textView: TextView,
    markdown: String,
    listener: RenderListener? = null
  ) {
    defaultEnhancedRenderer.render(markwon, textView, markdown, listener)
  }
  
  /**
   * 协程渲染Markdown
   */
  fun renderCoroutine(
    markwon: Markwon,
    textView: TextView,
    markdown: String,
    listener: RenderListener? = null
  ) {
    val config = RenderConfig(mode = RenderMode.COROUTINE)
    render(markwon, textView, markdown, config, listener)
  }
  
  /**
   * 取消所有活动的渲染任务
   */
  fun cancelAll() {
    defaultEnhancedRenderer.cancelAll()
  }
  
  /**
   * 清除渲染缓存
   */
  fun clearCache() {
    defaultEnhancedRenderer.clearCache()
  }
  
  /**
   * 获取渲染统计信息
   */
  fun getStats(): RendererStats {
    val cacheStats = defaultEnhancedRenderer.getCacheStats()
    val taskStats = defaultEnhancedRenderer.getActiveTaskStats()
    val schedulerStats = MarkdownScheduler.getSchedulerStats()
    
    return RendererStats(
      cacheSize = cacheStats.size,
      cacheEnabled = cacheStats.enabled,
      activeCoroutineJobs = taskStats.coroutineJobs,
      activeAsyncFutures = taskStats.asyncFutures,
      renderPoolActive = schedulerStats.renderPoolActive,
      imagePoolActive = schedulerStats.imagePoolActive
    )
  }
  
  /**
   * 渲染器统计信息
   */
  data class RendererStats(
    val cacheSize: Int,
    val cacheEnabled: Boolean,
    val activeCoroutineJobs: Int,
    val activeAsyncFutures: Int,
    val renderPoolActive: Boolean,
    val imagePoolActive: Boolean
  )

}
