package com.chenge.markdown.render

import android.content.Context
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.noties.markwon.Markwon

/**
 * MarkdownView：封装Markwon渲染
 */
class MarkdownView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

  private val markwon: Markwon = Markwon.create(context)

  fun setMarkdown(markdown: String) {
    markwon.setMarkdown(this, markdown)
  }

  fun setMarkdown(spanned: Spanned) {
    text = spanned
  }
}
