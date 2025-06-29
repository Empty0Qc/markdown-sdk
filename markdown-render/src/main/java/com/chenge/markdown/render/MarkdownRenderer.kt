package com.chenge.markdown.render

import android.widget.TextView
import io.noties.markwon.Markwon

/**
 * MarkdownRenderer：负责渲染Markdown
 */
object MarkdownRenderer {

  /**
   * 同步渲染Markdown（生产可用）
   */
  fun setMarkdownSync(markwon: Markwon, textView: TextView, markdown: String) {
    markwon.setMarkdown(textView, markdown)
  }

  /**
   * 异步渲染Markdown（生产可用）
   */
  fun setMarkdownAsync(markwon: Markwon, textView: TextView, markdown: String) {
    Thread {
      val node = markwon.parse(markdown)
      val spanned = markwon.render(node)
      textView.post {
        markwon.setParsedMarkdown(textView, spanned)
      }
    }.start()
  }
}
