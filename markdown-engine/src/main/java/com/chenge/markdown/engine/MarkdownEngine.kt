package com.chenge.markdown.engine

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.MarkdownConfigBuilder
import com.chenge.markdown.common.MarkdownSanitizer
import com.chenge.markdown.common.markdownConfig
import com.chenge.markdown.core.MarkdownParser
import com.chenge.markdown.plugins.MarkdownPlugins
import com.chenge.markdown.render.MarkdownRenderer
import com.chenge.markdown.render.MarkdownScheduler
import io.noties.markwon.Markwon

/**
 * MarkdownEngine：统一入口（moved to markdown-engine module）
 */
class MarkdownEngine private constructor(
  private val context: Context,
  private val config: MarkdownConfig,
  private val async: Boolean
) {

  private val engineMarkwon: Markwon by lazy {
    MarkwonPool.getOrCreate(context, config)
  }

  companion object {
    /**
     * 默认配置，同步渲染
     */
    fun with(context: Context): MarkdownEngine {
      return MarkdownEngine(context, MarkdownConfig(), async = false)
    }

    /**
     * 使用 DSL 配置创建 MarkdownEngine
     */
    fun with(context: Context, block: MarkdownConfigBuilder.() -> Unit): MarkdownEngine {
      val config = markdownConfig(block)
      return MarkdownEngine(context, config, async = config.asyncRendering)
    }

    /**
     * 使用预设配置创建 MarkdownEngine
     */
    fun withPreset(context: Context, preset: MarkdownConfig): MarkdownEngine {
      return MarkdownEngine(context, preset, async = preset.asyncRendering)
    }
  }

  /**
   * 启用异步渲染
   */
  fun async(): MarkdownEngine {
    return MarkdownEngine(context, config, async = true)
  }

  /**
   * 获取底层的 [Markwon] 实例，便于调试或自定义渲染
   */
  fun getMarkwon(): Markwon = engineMarkwon

  /**
   * 设置自定义配置
   */
  fun config(customConfig: MarkdownConfig): MarkdownEngine {
    return MarkdownEngine(context, customConfig, async = this.async)
  }

  /**
   * 渲染Markdown
   */
  fun render(target: TextView, markdown: String) {
    val startTime = if (config.debugMode) System.currentTimeMillis() else 0
    
    val parsed = MarkdownParser.parse(markdown)
    val safeMarkdown = MarkdownSanitizer.sanitize(parsed)
    
    if (async) {
      MarkdownRenderer.setMarkdownAsync(engineMarkwon, target, safeMarkdown)
    } else {
      MarkdownRenderer.setMarkdownSync(engineMarkwon, target, safeMarkdown)
    }
    
    if (config.debugMode) {
       val duration = System.currentTimeMillis() - startTime
       Log.d("MarkdownEngine", "渲染耗时: ${duration}ms, 异步: $async")
     }
  }
  
  /**
   * 流式渲染Markdown（逐步显示内容）
   * @deprecated 请使用ProgressiveRenderer替代此方法
   */
  @Deprecated("Use ProgressiveRenderer instead", ReplaceWith("ProgressiveRenderer"))
  fun renderStreaming(target: TextView, markdown: String) {
    val startTime = if (config.debugMode) System.currentTimeMillis() else 0
    
    val parsed = MarkdownParser.parse(markdown)
    val safeMarkdown = MarkdownSanitizer.sanitize(parsed)
    
    // 降级到异步渲染，建议使用ProgressiveRenderer
    MarkdownRenderer.setMarkdownAsync(engineMarkwon, target, safeMarkdown)
    
    if (config.debugMode) {
       val duration = System.currentTimeMillis() - startTime
       Log.d("MarkdownEngine", "流式渲染启动耗时: ${duration}ms")
     }
  }
  
  /**
   * 获取性能统计信息
   */
  fun getPerformanceStats(): PerformanceStats {
    return PerformanceStats(
      poolStats = MarkwonPool.getPoolStats(),
      schedulerStats = MarkdownScheduler.getSchedulerStats(),
      config = config
    )
  }
  
  /**
   * 清理资源（TextView 回收时调用）
   */
  fun cleanup() {
    // 清理过期的 Markwon 实例
    MarkwonPool.cleanupExpired()
    MarkdownScheduler.shutdown()
    ImageCacheManager.getInstance(context).clearAll()
    TextViewPool.getInstance().clear()
  }
  
  /**
   * 预热性能组件
   */
  fun warmUp() {
    // 预热 TextView 池
    TextViewPool.getInstance().warmUp(context)
    
    // 清理过期的图片缓存
    ImageCacheManager.getInstance(context).cleanupExpired()
  }
  
  /**
   * 获取缓存统计信息
   */
  fun getCacheStats(): CacheStats {
    val imageStats = ImageCacheManager.getInstance(context).getCacheStats()
    val textViewStats = TextViewPool.getInstance().getStats()
    val schedulerStats = MarkdownScheduler.getStats()
    
    return CacheStats(
      imageCache = imageStats,
      textViewPool = textViewStats,
      scheduler = schedulerStats
    )
  }
  
  data class PerformanceStats(
    val poolStats: MarkwonPool.PoolStats,
    val schedulerStats: MarkdownScheduler.SchedulerStats,
    val config: MarkdownConfig
  )
  
  data class CacheStats(
    val imageCache: ImageCacheManager.CacheStats,
    val textViewPool: TextViewPool.PoolStats,
    val scheduler: MarkdownScheduler.ThreadPoolStats
  )
}