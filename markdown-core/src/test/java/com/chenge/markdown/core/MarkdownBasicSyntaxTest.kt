package com.chenge.markdown.core

import org.junit.Test
import org.junit.Assert.*

/**
 * 基础 Markdown 语法测试
 * 验证 M1 模块的基础语法支持
 */
class MarkdownBasicSyntaxTest {
    
    @Test
    fun testHeaderSyntax() {
        // 测试标题语法识别
        assertTrue("H1 标题识别失败", MarkdownSyntax.Headers.isHeader("# 标题1"))
        assertTrue("H2 标题识别失败", MarkdownSyntax.Headers.isHeader("## 标题2"))
        assertTrue("H6 标题识别失败", MarkdownSyntax.Headers.isHeader("###### 标题6"))
        assertFalse("非标题误识别", MarkdownSyntax.Headers.isHeader("普通文本"))
        
        // 测试标题级别
        assertEquals("H1 级别错误", 1, MarkdownSyntax.Headers.getHeaderLevel("# 标题1"))
        assertEquals("H3 级别错误", 3, MarkdownSyntax.Headers.getHeaderLevel("### 标题3"))
        assertEquals("H6 级别错误", 6, MarkdownSyntax.Headers.getHeaderLevel("###### 标题6"))
        assertEquals("非标题级别应为0", 0, MarkdownSyntax.Headers.getHeaderLevel("普通文本"))
    }
    
    @Test
    fun testListSyntax() {
        // 测试无序列表
        assertTrue("无序列表识别失败", MarkdownSyntax.Lists.isUnorderedList("- 项目1"))
        assertTrue("无序列表识别失败", MarkdownSyntax.Lists.isUnorderedList("* 项目2"))
        assertTrue("无序列表识别失败", MarkdownSyntax.Lists.isUnorderedList("+ 项目3"))
        
        // 测试有序列表
        assertTrue("有序列表识别失败", MarkdownSyntax.Lists.isOrderedList("1. 第一项"))
        assertTrue("有序列表识别失败", MarkdownSyntax.Lists.isOrderedList("10. 第十项"))
        
        // 测试缩进级别
        assertEquals("缩进级别错误", 0, MarkdownSyntax.Lists.getListIndentLevel("- 项目"))
        assertEquals("缩进级别错误", 1, MarkdownSyntax.Lists.getListIndentLevel("  - 子项目"))
        assertEquals("缩进级别错误", 2, MarkdownSyntax.Lists.getListIndentLevel("    - 子子项目"))
    }
    
    @Test
    fun testCodeBlockSyntax() {
        // 测试围栏代码块
        assertTrue("代码块开始识别失败", MarkdownSyntax.CodeBlocks.isFencedCodeStart("```"))
        assertTrue("代码块开始识别失败", MarkdownSyntax.CodeBlocks.isFencedCodeStart("```kotlin"))
        assertTrue("代码块结束识别失败", MarkdownSyntax.CodeBlocks.isFencedCodeEnd("```"))
        
        // 测试代码语言
        assertEquals("代码语言识别错误", "kotlin", MarkdownSyntax.CodeBlocks.getCodeLanguage("```kotlin"))
        assertEquals("代码语言识别错误", "java", MarkdownSyntax.CodeBlocks.getCodeLanguage("```java"))
        assertNull("无语言代码块应返回null", MarkdownSyntax.CodeBlocks.getCodeLanguage("```"))
        
        // 测试行内代码
        assertTrue("行内代码识别失败", 
            MarkdownSyntax.CodeBlocks.INLINE_CODE_PATTERN.containsMatchIn("这是 `行内代码` 示例"))
    }
    
    @Test
    fun testBlockquoteSyntax() {
        // 测试引用块
        assertTrue("引用块识别失败", MarkdownSyntax.Blockquotes.isBlockquote("> 这是引用"))
        assertTrue("引用块识别失败", MarkdownSyntax.Blockquotes.isBlockquote(">引用无空格"))
        
        // 测试引用级别
        assertEquals("引用级别错误", 1, MarkdownSyntax.Blockquotes.getQuoteLevel("> 一级引用"))
        assertEquals("引用级别错误", 2, MarkdownSyntax.Blockquotes.getQuoteLevel("> > 二级引用"))
        assertEquals("引用级别错误", 3, MarkdownSyntax.Blockquotes.getQuoteLevel("> > > 三级引用"))
    }
    
    @Test
    fun testLinkSyntax() {
        val textWithLinks = "访问 [Google](https://google.com) 或 <https://github.com>"
        assertTrue("链接识别失败", MarkdownSyntax.Links.hasLinks(textWithLinks))
        
        val textWithoutLinks = "这是普通文本"
        assertFalse("非链接误识别", MarkdownSyntax.Links.hasLinks(textWithoutLinks))
    }
    
    @Test
    fun testImageSyntax() {
        val textWithImages = "![图片描述](https://example.com/image.png)"
        assertTrue("图片识别失败", MarkdownSyntax.Images.hasImages(textWithImages))
        
        val textWithoutImages = "这是普通文本"
        assertFalse("非图片误识别", MarkdownSyntax.Images.hasImages(textWithoutImages))
    }
    
    @Test
    fun testEmphasisSyntax() {
        // 测试粗体
        assertTrue("粗体识别失败", 
            MarkdownSyntax.Emphasis.BOLD_PATTERN.containsMatchIn("**粗体文本**"))
        assertTrue("粗体识别失败", 
            MarkdownSyntax.Emphasis.BOLD_PATTERN.containsMatchIn("__粗体文本__"))
        
        // 测试斜体
        assertTrue("斜体识别失败", 
            MarkdownSyntax.Emphasis.ITALIC_PATTERN.containsMatchIn("*斜体文本*"))
        assertTrue("斜体识别失败", 
            MarkdownSyntax.Emphasis.ITALIC_PATTERN.containsMatchIn("_斜体文本_"))
        
        // 测试删除线
        assertTrue("删除线识别失败", 
            MarkdownSyntax.Emphasis.STRIKETHROUGH_PATTERN.containsMatchIn("~~删除文本~~"))
    }
    
    @Test
    fun testHorizontalRuleSyntax() {
        assertTrue("水平线识别失败", MarkdownSyntax.HorizontalRules.isHorizontalRule("---"))
        assertTrue("水平线识别失败", MarkdownSyntax.HorizontalRules.isHorizontalRule("***"))
        assertTrue("水平线识别失败", MarkdownSyntax.HorizontalRules.isHorizontalRule("___"))
        assertTrue("水平线识别失败", MarkdownSyntax.HorizontalRules.isHorizontalRule("-----"))
        
        assertFalse("非水平线误识别", MarkdownSyntax.HorizontalRules.isHorizontalRule("--"))
        assertFalse("非水平线误识别", MarkdownSyntax.HorizontalRules.isHorizontalRule("普通文本"))
    }
    
    @Test
    fun testMarkdownParserBasicFunctionality() {
        val simpleMarkdown = "# 标题\n\n这是段落。"
        val result = MarkdownParser.parseWithInfo(simpleMarkdown)
        
        assertTrue("解析应该成功", result.isValid)
        assertTrue("应该识别标题", result.syntaxInfo.hasHeaders)
        assertEquals("复杂度应为简单", MarkdownParser.ComplexityLevel.SIMPLE, result.syntaxInfo.estimatedComplexity)
    }
    
    @Test
    fun testComplexMarkdownParsing() {
        val complexMarkdown = """
            # 主标题
            
            这是一个段落，包含 **粗体** 和 *斜体* 文本。
            
            ## 子标题
            
            - 无序列表项1
            - 无序列表项2
              - 嵌套项
            
            1. 有序列表项1
            2. 有序列表项2
            
            > 这是引用块
            > > 嵌套引用
            
            ```kotlin
            fun hello() = "world"
            ```
            
            ![图片](https://example.com/image.png)
            
            [链接](https://example.com)
            
            ---
        """.trimIndent()
        
        val result = MarkdownParser.parseWithInfo(complexMarkdown)
        
        assertTrue("解析应该成功", result.isValid)
        assertTrue("应该识别标题", result.syntaxInfo.hasHeaders)
        assertTrue("应该识别列表", result.syntaxInfo.hasList)
        assertTrue("应该识别代码块", result.syntaxInfo.hasCodeBlocks)
        assertTrue("应该识别引用块", result.syntaxInfo.hasBlockquotes)
        assertTrue("应该识别图片", result.syntaxInfo.hasImages)
        assertTrue("应该识别链接", result.syntaxInfo.hasLinks)
        assertTrue("应该识别强调", result.syntaxInfo.hasEmphasis)
        assertEquals("复杂度应为复杂", MarkdownParser.ComplexityLevel.COMPLEX, result.syntaxInfo.estimatedComplexity)
    }
    
    @Test
    fun testMarkdownValidator() {
        val validator = MarkdownValidator()
        val result = validator.validateBasicSyntax()
        
        assertTrue("基础语法验证应该通过", result.isValid)
        assertTrue("应该支持标题", result.supportedFeatures.contains("标题"))
        assertTrue("应该支持段落", result.supportedFeatures.contains("段落"))
        assertTrue("应该支持列表", result.supportedFeatures.contains("无序列表"))
        assertTrue("应该支持代码块", result.supportedFeatures.contains("代码块"))
        
        // 验证报告生成
        val report = validator.generateValidationReport()
        assertTrue("报告应包含验证状态", report.contains("验证状态"))
        assertTrue("报告应包含支持特性", report.contains("支持的特性"))
    }
    
    @Test
    fun testMarkdownSyntaxDetection() {
        // 测试包含Markdown语法的文本
        val markdownText = "# 标题\n\n- 列表项\n\n**粗体**"
        assertTrue("应该检测到Markdown语法", MarkdownParser.hasMarkdownSyntax(markdownText))
        
        // 测试纯文本
        val plainText = "这是普通文本，没有任何Markdown语法。"
        assertFalse("不应该检测到Markdown语法", MarkdownParser.hasMarkdownSyntax(plainText))
    }
}