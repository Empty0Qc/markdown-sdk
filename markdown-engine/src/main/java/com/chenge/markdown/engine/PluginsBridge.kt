package com.chenge.markdown.engine

import android.content.Context
import com.chenge.markdown.common.MarkdownConfig
import io.noties.markwon.Markwon

// 对外重导出插件类型，调用方仅需依赖 engine
typealias MarkdownPlugin = com.chenge.markdown.plugins.MarkdownPlugin
typealias ClickablePlugin = com.chenge.markdown.plugins.ClickablePlugin
typealias ImageSizePlugin = com.chenge.markdown.plugins.ImageSizePlugin

/**
 * 插件注册与创建的门面，转发到 markdown-plugins
 */
object MarkdownPlugins {
    fun register(plugin: MarkdownPlugin) {
        com.chenge.markdown.plugins.MarkdownPlugins.register(plugin)
    }

    fun create(context: Context, config: MarkdownConfig): Markwon {
        return com.chenge.markdown.plugins.MarkdownPlugins.create(context, config)
    }
}