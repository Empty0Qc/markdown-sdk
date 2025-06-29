package com.chenge.markdown.plugins

import io.noties.markwon.Markwon

/**
 * 插件统一接口，外部可实现并通过 [MarkdownPlugins.register] 注册
 */
interface MarkdownPlugin {
    fun apply(builder: Markwon.Builder)
}
