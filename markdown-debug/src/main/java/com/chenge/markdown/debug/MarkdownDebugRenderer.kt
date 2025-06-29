package com.chenge.markdown.debug

import android.widget.TextView
import io.noties.markwon.Markwon

/**
 * MarkdownDebugRenderer：仅用于调试
 */
object MarkdownDebugRenderer {

  /**
   * 同步渲染+耗时
   */
  fun setMarkdownSyncWithTiming(
    markwon: Markwon,
    textView: TextView,
    markdown: String,
    onDurationMeasured: (Long) -> Unit
  ) {
    val start = System.currentTimeMillis()
    markwon.setMarkdown(textView, markdown)
    val duration = System.currentTimeMillis() - start
    onDurationMeasured(duration)
  }

  /**
   * 异步渲染+耗时
   */
  fun setMarkdownAsyncWithTiming(
    markwon: Markwon,
    textView: TextView,
    markdown: String,
    onDurationMeasured: (Long) -> Unit
  ) {
    val start = System.currentTimeMillis()
    Thread {
      val node = markwon.parse(markdown)
      val spanned = markwon.render(node)
      val duration = System.currentTimeMillis() - start
      textView.post {
        markwon.setParsedMarkdown(textView, spanned)
        onDurationMeasured(duration)
      }
    }.start()
  }
}
