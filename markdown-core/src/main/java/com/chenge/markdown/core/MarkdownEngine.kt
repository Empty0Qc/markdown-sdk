package com.chenge.markdown.core

import android.content.Context
import android.widget.TextView
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.MarkdownSanitizer
import com.chenge.markdown.plugins.MarkdownPlugins
import com.chenge.markdown.render.MarkdownRenderer
import io.noties.markwon.Markwon

/**
 * MarkdownEngine：统一入口
 */
class MarkdownEngine private constructor(
  private val context: Context,
  private val config: MarkdownConfig,
  private val async: Boolean
) {

  private val markwon: Markwon by lazy {
    MarkdownPlugins.create(context, config)
  }

  companion object {
    /**
     * 默认配置，同步渲染
     */
    fun with(context: Context): MarkdownEngine {
      return MarkdownEngine(context, MarkdownConfig(), async = false)
    }
  }

  /**
   * 启用异步渲染
   */
  fun async(): MarkdownEngine {
    return MarkdownEngine(context, config, async = true)
  }

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
    val safeMarkdown = MarkdownSanitizer.sanitize(markdown)
    if (async) {
      MarkdownRenderer.setMarkdownAsync(markwon, target, safeMarkdown)
    } else {
      MarkdownRenderer.setMarkdownSync(markwon, target, safeMarkdown)
    }
  }
}
