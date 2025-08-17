package com.chenge.markdown.plugins

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import io.noties.markwon.syntax.SyntaxHighlight
import java.util.regex.Pattern

/**
 * 一个简单的关键字高亮实现，仅用于演示。
 * 支持 Kotlin/Java 常见关键字。
 */
class SimpleSyntaxHighlight(
    private val keywordColor: Int = Color.parseColor("#CC7832")
) : SyntaxHighlight {
    private val pattern = Pattern.compile("\\b(fun|val|var|if|else|for|while|return|class|object|import|package)\\b")

    override fun highlight(language: String?, code: String): CharSequence {
        val spannable = SpannableString(code)
        val matcher = pattern.matcher(code)
        while (matcher.find()) {
            spannable.setSpan(
                ForegroundColorSpan(keywordColor),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}
