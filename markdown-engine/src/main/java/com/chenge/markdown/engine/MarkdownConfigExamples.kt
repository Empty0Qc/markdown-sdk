package com.chenge.markdown.engine

import android.content.Context
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.markdownConfig

/**
 * MarkdownConfig 使用示例
 * 展示各种配置方式和预设
 */
object MarkdownConfigExamples {

    /**
     * 基本用法示例
     */
    fun basicUsage(context: Context) {
        // 1. 默认配置
        val engine1 = MarkdownEngine.with(context)

        // 2. 使用预设配置
        val engine2 = MarkdownEngine.withPreset(context, MarkdownConfig.blog())
        val engine3 = MarkdownEngine.withPreset(context, MarkdownConfig.chat())
        val engine4 = MarkdownEngine.withPreset(context, MarkdownConfig.editor())

        // 3. 使用 DSL 配置
        val engine5 = MarkdownEngine.with(context) {
            enableAll()
            async()
            debug()
            imageSize(1200, 800)
        }
    }

    /**
     * DSL 配置示例
     */
    fun dslExamples(context: Context) {
        // 博客文章配置
        val blogEngine = MarkdownEngine.with(context) {
            tables()
            taskLists()
            latex()
            safeMode() // 禁用 HTML
            imageSize(800, 600)
        }

        // 聊天消息配置
        val chatEngine = MarkdownEngine.with(context) {
            disableAll()
            enableImageLoading = true
            enableLinkClick = true
            async()
        }

        // 富文本编辑器配置
        val editorEngine = MarkdownEngine.with(context) {
            enableAll()
            debug()
            plugin("custom-highlight")
            plugin("custom-emoji")
        }

        // 性能优化配置
        val performanceEngine = MarkdownEngine.with(context) {
            disableAll()
            async()
            enableImageLoading = false // 禁用图片加载以提升性能
        }
    }

    /**
     * 自定义配置示例
     */
    fun customConfig(context: Context) {
        val customConfig = markdownConfig {
            // 基础功能
            enableTables = true
            enableTaskList = true

            // 图片配置
            enableImageLoading = true
            imageSize(1024, 768)

            // 性能配置
            async()

            // 安全配置
            safeMode()

            // 自定义插件
            plugin("syntax-highlight")
            plugin("math-formula")
        }

        val engine = MarkdownEngine.withPreset(context, customConfig)
    }

    /**
     * 条件配置示例
     */
    fun conditionalConfig(context: Context, isDebug: Boolean, enableAdvanced: Boolean) {
        val engine = MarkdownEngine.with(context) {
            // 基础配置
            tables()
            taskLists()

            // 条件配置
            if (isDebug) {
                debug()
            }

            if (enableAdvanced) {
                latex()
                enableHtml = true
            } else {
                safeMode()
            }

            // 性能配置
            async()
            imageSize(800, 600)
        }
    }
}
