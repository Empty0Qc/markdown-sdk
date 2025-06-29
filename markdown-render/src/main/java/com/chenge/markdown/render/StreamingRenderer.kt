package com.chenge.markdown.render

import android.widget.TextView
import io.noties.markwon.Markwon
import org.commonmark.parser.Parser

/**
 * 流式渲染：逐行解析并更新 TextView
 */
object StreamingRenderer {

    fun renderIncrementally(markwon: Markwon, textView: TextView, markdown: String) {
        Thread {
            val parser = Parser.builder().build()
            val lines = markdown.lines()
            val buffer = StringBuilder()
            for (line in lines) {
                buffer.appendLine(line)
                val node = parser.parse(buffer.toString())
                val spanned = markwon.render(node)
                textView.post { markwon.setParsedMarkdown(textView, spanned) }
            }
        }.start()
    }
}
