package com.chenge.markdown.plugins

import android.os.Build
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.annotation.RequiresApi
import com.chenge.markdown.common.MarkdownStyleConfig
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import org.commonmark.node.Code
import org.commonmark.node.Heading
import org.commonmark.node.Link

/**
 * 根据 [MarkdownStyleConfig] 自定义样式
 */
class StylePlugin(private val style: MarkdownStyleConfig) : MarkdownPlugin {
    override fun apply(builder: Markwon.Builder) {
        builder.usePlugin(object : AbstractMarkwonPlugin() {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                // Heading: 自定义Typeface +颜色
                builder.appendFactory(Heading::class.java) { _, _ ->
                  style.headingTypeface?.let {
                    arrayOf(
                      TypefaceSpan(it),
                      ForegroundColorSpan(style.headingColor)
                    )
                  }
                }

                // Link: 颜色
                builder.appendFactory(Link::class.java) { _, _ ->
                    arrayOf(
                        ForegroundColorSpan(style.linkColor)
                    )
                }

                // Code: 背景色
                builder.appendFactory(Code::class.java) { _, _ ->
                    arrayOf(
                        BackgroundColorSpan(style.codeBackgroundColor)
                    )
                }
            }
        })
    }
}
