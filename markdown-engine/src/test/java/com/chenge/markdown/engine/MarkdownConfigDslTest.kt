package com.chenge.markdown.engine

import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.CodeHighlight
import com.chenge.markdown.common.markdownConfig
import org.junit.Test
import org.junit.Assert.*

/**
 * MarkdownConfig DSL 功能测试
 */
class MarkdownConfigDslTest {

    @Test
    fun testDefaultConfig() {
        val config = MarkdownConfig()
        assertFalse(config.enableHtml)
        assertTrue(config.enableTables)
        assertTrue(config.enableTaskList)
        assertFalse(config.enableLatex)
        assertTrue(config.enableImageLoading)
        assertTrue(config.enableLinkClick)
        assertFalse(config.asyncRendering)
        assertFalse(config.debugMode)
        assertEquals(CodeHighlight.NONE, config.codeHighlight)
    }

    @Test
    fun testPresetConfigs() {
        // 测试博客配置
        val blogConfig = MarkdownConfig.blog()
        assertFalse(blogConfig.enableHtml)
        assertTrue(blogConfig.enableTables)
        assertTrue(blogConfig.enableTaskList)
        assertTrue(blogConfig.enableLatex)
        assertTrue(blogConfig.enableImageLoading)
        assertTrue(blogConfig.enableLinkClick)
        assertEquals(CodeHighlight.PRISM_LIGHT, blogConfig.codeHighlight)

        // 测试聊天配置
        val chatConfig = MarkdownConfig.chat()
        assertFalse(chatConfig.enableHtml)
        assertFalse(chatConfig.enableTables)
        assertFalse(chatConfig.enableTaskList)
        assertFalse(chatConfig.enableLatex)
        assertTrue(chatConfig.enableImageLoading)
        assertTrue(chatConfig.enableLinkClick)
        assertTrue(chatConfig.asyncRendering)
        assertEquals(CodeHighlight.NONE, chatConfig.codeHighlight)

        // 测试编辑器配置
        val editorConfig = MarkdownConfig.editor()
        assertTrue(editorConfig.enableHtml)
        assertTrue(editorConfig.enableTables)
        assertTrue(editorConfig.enableTaskList)
        assertTrue(editorConfig.enableLatex)
        assertTrue(editorConfig.enableImageLoading)
        assertTrue(editorConfig.enableLinkClick)
        assertTrue(editorConfig.debugMode)
        assertEquals(CodeHighlight.PRISM_DARK, editorConfig.codeHighlight)
    }

    @Test
    fun testDslConfiguration() {
        val config = markdownConfig {
            enableAll()
            async()
            debug()
            imageSize(1200, 800)
            plugin("test-plugin")
        }

        assertTrue(config.enableHtml)
        assertTrue(config.enableTables)
        assertTrue(config.enableTaskList)
        assertTrue(config.enableLatex)
        assertTrue(config.enableImageLoading)
        assertTrue(config.enableLinkClick)
        assertTrue(config.asyncRendering)
        assertTrue(config.debugMode)
        assertEquals(1200, config.maxImageWidth)
        assertEquals(800, config.maxImageHeight)
        assertTrue(config.customPlugins.contains("test-plugin"))
        assertEquals(CodeHighlight.NONE, config.codeHighlight)
    }

    @Test
    fun testDslDisableAll() {
        val config = markdownConfig {
            disableAll()
        }

        assertFalse(config.enableHtml)
        assertFalse(config.enableTables)
        assertFalse(config.enableTaskList)
        assertFalse(config.enableLatex)
        assertFalse(config.enableImageLoading)
        assertFalse(config.enableLinkClick)
    }

    @Test
    fun testDslIndividualFeatures() {
        val config = markdownConfig {
            tables()
            taskLists()
            latex()
            safeMode()
        }

        assertFalse(config.enableHtml) // safeMode() 禁用 HTML
        assertTrue(config.enableTables)
        assertTrue(config.enableTaskList)
        assertTrue(config.enableLatex)
    }

    @Test
    fun testDslImageConfiguration() {
        val config = markdownConfig {
            imageSize(1024, 768)
        }

        assertEquals(1024, config.maxImageWidth)
        assertEquals(768, config.maxImageHeight)
    }

    @Test
    fun testDslPluginConfiguration() {
        val config = markdownConfig {
            plugin("plugin1")
            plugin("plugin2")
            plugin("plugin3")
        }

        assertEquals(3, config.customPlugins.size)
        assertTrue(config.customPlugins.contains("plugin1"))
        assertTrue(config.customPlugins.contains("plugin2"))
        assertTrue(config.customPlugins.contains("plugin3"))
    }
}