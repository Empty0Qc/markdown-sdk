package com.chenge.markdown.render

import android.util.Log
import android.widget.TextView
import com.chenge.markdown.render.MarkdownScheduler
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
  

}
