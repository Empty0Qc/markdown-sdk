package com.chenge.markdown.plugins

import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Link

/**
 * 为链接、块级代码、行内代码提供点击回调
 */
class ClickablePlugin(
    private val onLinkClick: (String) -> Unit,
    private val onCodeClick: ((String) -> Unit)? = null
) : MarkdownPlugin {

    override fun apply(builder: Markwon.Builder) {
        builder.usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureSpansFactory(factoryBuilder: MarkwonSpansFactory.Builder) {

                // 链接
                factoryBuilder.setFactory(Link::class.java) { _, props ->
                    val link = io.noties.markwon.core.CoreProps.LINK_DESTINATION.get(props) ?: ""
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onLinkClick(link)
                        }
                    }
                }

                // 块级代码
                factoryBuilder.appendFactory(FencedCodeBlock::class.java) { _, _ ->
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val textView = widget as? TextView
                            val selStart = textView?.selectionStart ?: -1
                            val selEnd = textView?.selectionEnd ?: -1
                            val clickedText = if (selStart >= 0 && selEnd >= 0 && selEnd > selStart) {
                                textView?.text?.substring(selStart, selEnd)
                            } else {
                                textView?.text?.toString() ?: ""
                            } ?: return
                            onCodeClick?.invoke(clickedText)
                        }
                    }
                }

                // 行内代码
                factoryBuilder.appendFactory(Code::class.java) { _, _ ->
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            val textView = widget as? TextView
                            val selStart = textView?.selectionStart ?: -1
                            val selEnd = textView?.selectionEnd ?: -1
                            val clickedText = if (selStart >= 0 && selEnd >= 0 && selEnd > selStart) {
                                textView?.text?.substring(selStart, selEnd)
                            } else {
                                textView?.text?.toString() ?: ""
                            } ?: return
                            onCodeClick?.invoke(clickedText)
                        }
                    }
                }
            }
        })
    }
}
