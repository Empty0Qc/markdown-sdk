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
  
  /**
   * 流式渲染Markdown（逐步显示内容）
   */
  fun setMarkdownStreaming(markwon: Markwon, textView: TextView, markdown: String) {
    MarkdownScheduler.asyncRender(
      backgroundTask = {
        val node = markwon.parse(markdown)
        markwon.render(node)
      },
      onResult = { spanned ->
        // 流式显示：先显示部分内容，然后逐步完善
        textView.text = ""
        animateTextAppearance(textView, spanned.toString())
      },
      onError = { error ->
        Log.e("MarkdownRenderer", "流式渲染失败", error)
        setMarkdownSync(markwon, textView, markdown)
      }
    )
  }
  
  /**
   * 文本逐步显示动画
   */
  private fun animateTextAppearance(textView: TextView, fullText: String) {
    val chunkSize = 50 // 每次显示的字符数
    var currentIndex = 0
    
    fun showNextChunk() {
      if (currentIndex < fullText.length) {
        val endIndex = minOf(currentIndex + chunkSize, fullText.length)
        textView.text = fullText.substring(0, endIndex)
        currentIndex = endIndex
        
        MarkdownScheduler.executeOnMainDelayed(::showNextChunk, 16) // ~60fps
      }
    }
    
    showNextChunk()
  }
}
