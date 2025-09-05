package com.chenge.markdown.core

import com.chenge.markdown.common.MarkdownSanitizer

/**
 * MarkdownParser: 增强的基础解析器
 * 支持基础 Markdown 语法解析和验证
 */
object MarkdownParser {

    /**
     * 解析结果
     */
    data class ParseResult(
        val content: String,
        val syntaxInfo: SyntaxInfo,
        val isValid: Boolean = true,
        val errors: List<String> = emptyList()
    )
    
    /**
     * 语法信息数据类
     */
    data class SyntaxInfo(
        val hasHeaders: Boolean = false,
        val hasList: Boolean = false,
        val hasCodeBlocks: Boolean = false,
        val hasBlockquotes: Boolean = false,
        val hasImages: Boolean = false,
        val hasLinks: Boolean = false,
        val hasEmphasis: Boolean = false,
        val estimatedComplexity: ComplexityLevel = ComplexityLevel.SIMPLE,
        // 扩展语法支持
        val hasTables: Boolean = false,
        val hasTaskLists: Boolean = false,
        val hasNestedBlocks: Boolean = false,
        val hasStrikethrough: Boolean = false,
        val hasAutoLinks: Boolean = false,
        val extendedSyntaxInfo: MarkdownExtendedSyntax.ExtendedSyntaxInfo? = null
    )
    
    /**
     * 复杂度级别
     */
    enum class ComplexityLevel {
        SIMPLE,    // 纯文本或简单格式
        MODERATE,  // 包含基础语法
        COMPLEX    // 包含复杂语法和嵌套
    }

    /**
     * 解析Markdown文本
     * @param input 原始Markdown字符串
     * @return 解析后的安全字符串
     */
    fun parse(input: String): String {
        val result = parseWithInfo(input)
        return result.content
    }
    
    /**
     * 解析Markdown文本并返回详细信息
     * @param input 原始Markdown字符串
     * @return 包含语法信息的解析结果
     */
    fun parseWithInfo(input: String): ParseResult {
        try {
            // 1. Emoji 替换
            val markdownWithEmoji = EmojiReplacer.replaceShortcodes(input)
            
            // 2. 安全性处理
            val safeMarkdown = MarkdownSanitizer.sanitize(markdownWithEmoji)
            
            // 3. 语法分析
            val syntaxInfo = analyzeSyntax(safeMarkdown)
            
            return ParseResult(
                content = safeMarkdown,
                syntaxInfo = syntaxInfo,
                isValid = true
            )
        } catch (e: Exception) {
            return ParseResult(
                content = input,
                syntaxInfo = SyntaxInfo(),
                isValid = false,
                errors = listOf("解析错误: ${e.message}")
            )
        }
    }
    
    /**
     * 分析Markdown语法
     */
    private fun analyzeSyntax(content: String): SyntaxInfo {
        val lines = content.lines()
        
        // 基础语法分析
        val hasHeaders = lines.any { MarkdownSyntax.Headers.isHeader(it) }
        val hasList = lines.any { MarkdownSyntax.Lists.isList(it) }
        val hasCodeBlocks = lines.any { MarkdownSyntax.CodeBlocks.isCodeBlock(it) }
        val hasBlockquotes = lines.any { MarkdownSyntax.Blockquotes.isBlockquote(it) }
        val hasImages = MarkdownSyntax.Images.hasImages(content)
        val hasLinks = MarkdownSyntax.Links.hasLinks(content)
        val hasEmphasis = MarkdownSyntax.Emphasis.hasEmphasis(content)
        
        // 扩展语法分析
        val extendedInfo = MarkdownExtendedSyntax.analyzeExtendedSyntax(content)
        val hasTables = extendedInfo.hasTables
        val hasTaskLists = extendedInfo.hasTaskLists
        val hasNestedBlocks = extendedInfo.hasNestedBlocks
        val hasStrikethrough = extendedInfo.hasStrikethrough
        val hasAutoLinks = extendedInfo.hasAutoLinks
        
        val complexity = calculateComplexity(
            hasHeaders, hasList, hasCodeBlocks, hasImages, 
            hasLinks, hasEmphasis, hasBlockquotes, lines.size
        )
        
        return SyntaxInfo(
            hasHeaders = hasHeaders,
            hasList = hasList,
            hasCodeBlocks = hasCodeBlocks,
            hasImages = hasImages,
            hasLinks = hasLinks,
            hasEmphasis = hasEmphasis,
            hasBlockquotes = hasBlockquotes,
            estimatedComplexity = complexity,
            hasTables = hasTables,
            hasTaskLists = hasTaskLists,
            hasNestedBlocks = hasNestedBlocks,
            hasStrikethrough = hasStrikethrough,
            hasAutoLinks = hasAutoLinks,
            extendedSyntaxInfo = extendedInfo
        )
    }
    
    /**
     * 计算内容复杂度
     */
    private fun calculateComplexity(
        hasHeaders: Boolean,
        hasList: Boolean, 
        hasCodeBlocks: Boolean,
        hasImages: Boolean,
        hasLinks: Boolean,
        hasEmphasis: Boolean,
        hasBlockquotes: Boolean,
        lineCount: Int,
        hasTables: Boolean = false,
        hasTaskLists: Boolean = false,
        hasNestedBlocks: Boolean = false
    ): ComplexityLevel {
        var score = 0
        
        // 基础语法评分
        if (hasHeaders) score += 1
        if (hasList) score += 1
        if (hasCodeBlocks) score += 2
        if (hasImages) score += 2
        if (hasLinks) score += 1
        if (hasEmphasis) score += 1
        if (hasBlockquotes) score += 1
        
        // 扩展语法评分
        if (hasTables) score += 3  // 表格相对复杂
        if (hasTaskLists) score += 1
        if (hasNestedBlocks) score += 2  // 嵌套块增加复杂度
        
        if (lineCount > 50) score += 2
        else if (lineCount > 20) score += 1
        
        return when {
            score <= 2 -> ComplexityLevel.SIMPLE
            score <= 6 -> ComplexityLevel.MODERATE  // 调整阈值以适应扩展语法
            else -> ComplexityLevel.COMPLEX
        }
    }
    
    /**
     * 验证Markdown语法
     */
    fun validateSyntax(input: String): MarkdownValidator.ValidationResult {
        val validator = MarkdownValidator()
        return validator.validateBasicSyntax()
    }
    
    /**
     * 检查是否包含Markdown语法
     */
    fun hasMarkdownSyntax(input: String): Boolean {
        return MarkdownSyntax.hasMarkdownSyntax(input)
    }
}
