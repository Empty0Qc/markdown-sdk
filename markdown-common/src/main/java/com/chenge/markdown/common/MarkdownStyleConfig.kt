package com.chenge.markdown.common

import android.graphics.Typeface
import android.graphics.Color

/**
 * 样式配置，可根据节点类型自定义字体和颜色
 */
data class MarkdownStyleConfig(
    val headingColor: Int = Color.BLACK,
    val codeBackgroundColor: Int = Color.LTGRAY,
    val linkColor: Int = Color.BLUE,
    val headingTypeface: Typeface? = null
)
