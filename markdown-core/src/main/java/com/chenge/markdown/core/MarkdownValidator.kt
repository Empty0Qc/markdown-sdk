package com.chenge.markdown.core

/**
 * Markdown 语法验证器
 * 用于验证基础语法支持的完整性
 */
class MarkdownValidator {
    
    /**
     * 验证结果
     */
    data class ValidationResult(
        val isValid: Boolean,
        val supportedFeatures: List<String>,
        val unsupportedFeatures: List<String>,
        val errors: List<String> = emptyList()
    )
    
    /**
     * 支持的基础语法特性
     */
    enum class SyntaxFeature(val displayName: String, val testPattern: String) {
        HEADERS("标题", "# 标题1\n## 标题2\n### 标题3"),
        PARAGRAPHS("段落", "这是一个段落。\n\n这是另一个段落。"),
        UNORDERED_LISTS("无序列表", "- 项目1\n- 项目2\n  - 子项目"),
        ORDERED_LISTS("有序列表", "1. 第一项\n2. 第二项\n   1. 子项目"),
        CODE_BLOCKS("代码块", "```kotlin\nfun hello() = \"world\"\n```"),
        INLINE_CODE("行内代码", "这是 `行内代码` 示例"),
        BLOCKQUOTES("引用块", "> 这是引用\n> > 嵌套引用"),
        LINKS("链接", "[链接文本](https://example.com)"),
        IMAGES("图片", "![图片描述](https://example.com/image.png)"),
        BOLD("粗体", "**粗体文本** 和 __另一种粗体__"),
        ITALIC("斜体", "*斜体文本* 和 _另一种斜体_"),
        STRIKETHROUGH("删除线", "~~删除的文本~~"),
        HORIZONTAL_RULES("水平分割线", "---\n***\n___")
    }
    
    /**
     * 验证所有基础语法支持
     */
    fun validateBasicSyntax(): ValidationResult {
        val supportedFeatures = mutableListOf<String>()
        val unsupportedFeatures = mutableListOf<String>()
        val errors = mutableListOf<String>()
        
        SyntaxFeature.values().forEach { feature ->
            try {
                val isSupported = when (feature) {
                    SyntaxFeature.HEADERS -> validateHeaders(feature.testPattern)
                    SyntaxFeature.PARAGRAPHS -> validateParagraphs(feature.testPattern)
                    SyntaxFeature.UNORDERED_LISTS -> validateUnorderedLists(feature.testPattern)
                    SyntaxFeature.ORDERED_LISTS -> validateOrderedLists(feature.testPattern)
                    SyntaxFeature.CODE_BLOCKS -> validateCodeBlocks(feature.testPattern)
                    SyntaxFeature.INLINE_CODE -> validateInlineCode(feature.testPattern)
                    SyntaxFeature.BLOCKQUOTES -> validateBlockquotes(feature.testPattern)
                    SyntaxFeature.LINKS -> validateLinks(feature.testPattern)
                    SyntaxFeature.IMAGES -> validateImages(feature.testPattern)
                    SyntaxFeature.BOLD -> validateBold(feature.testPattern)
                    SyntaxFeature.ITALIC -> validateItalic(feature.testPattern)
                    SyntaxFeature.STRIKETHROUGH -> validateStrikethrough(feature.testPattern)
                    SyntaxFeature.HORIZONTAL_RULES -> validateHorizontalRules(feature.testPattern)
                }
                
                if (isSupported) {
                    supportedFeatures.add(feature.displayName)
                } else {
                    unsupportedFeatures.add(feature.displayName)
                }
            } catch (e: Exception) {
                errors.add("验证 ${feature.displayName} 时出错: ${e.message}")
                unsupportedFeatures.add(feature.displayName)
            }
        }
        
        return ValidationResult(
            isValid = unsupportedFeatures.isEmpty() && errors.isEmpty(),
            supportedFeatures = supportedFeatures,
            unsupportedFeatures = unsupportedFeatures,
            errors = errors
        )
    }
    
    private fun validateHeaders(testPattern: String): Boolean {
        val lines = testPattern.lines()
        return lines.any { MarkdownSyntax.Headers.isHeader(it) } &&
               MarkdownSyntax.Headers.getHeaderLevel("# 标题1") == 1 &&
               MarkdownSyntax.Headers.getHeaderLevel("## 标题2") == 2 &&
               MarkdownSyntax.Headers.getHeaderLevel("### 标题3") == 3
    }
    
    private fun validateParagraphs(testPattern: String): Boolean {
        val lines = testPattern.lines().filter { it.isNotBlank() }
        return lines.any { MarkdownSyntax.Paragraphs.isParagraph(it) }
    }
    
    private fun validateUnorderedLists(testPattern: String): Boolean {
        return MarkdownSyntax.Lists.isUnorderedList("- 项目1") &&
               MarkdownSyntax.Lists.getListIndentLevel("  - 子项目") == 1
    }
    
    private fun validateOrderedLists(testPattern: String): Boolean {
        return MarkdownSyntax.Lists.isOrderedList("1. 第一项") &&
               MarkdownSyntax.Lists.getListIndentLevel("   1. 子项目") == 1
    }
    
    private fun validateCodeBlocks(testPattern: String): Boolean {
        return MarkdownSyntax.CodeBlocks.isFencedCodeStart("```kotlin") &&
               MarkdownSyntax.CodeBlocks.getCodeLanguage("```kotlin") == "kotlin" &&
               MarkdownSyntax.CodeBlocks.isFencedCodeEnd("```")
    }
    
    private fun validateInlineCode(testPattern: String): Boolean {
        return MarkdownSyntax.CodeBlocks.INLINE_CODE_PATTERN.containsMatchIn(testPattern)
    }
    
    private fun validateBlockquotes(testPattern: String): Boolean {
        return MarkdownSyntax.Blockquotes.isBlockquote("> 这是引用") &&
               MarkdownSyntax.Blockquotes.getQuoteLevel("> > 嵌套引用") == 2
    }
    
    private fun validateLinks(testPattern: String): Boolean {
        return MarkdownSyntax.Links.hasLinks(testPattern)
    }
    
    private fun validateImages(testPattern: String): Boolean {
        return MarkdownSyntax.Images.hasImages(testPattern)
    }
    
    private fun validateBold(testPattern: String): Boolean {
        return MarkdownSyntax.Emphasis.BOLD_PATTERN.containsMatchIn(testPattern)
    }
    
    private fun validateItalic(testPattern: String): Boolean {
        return MarkdownSyntax.Emphasis.ITALIC_PATTERN.containsMatchIn(testPattern)
    }
    
    private fun validateStrikethrough(testPattern: String): Boolean {
        return MarkdownSyntax.Emphasis.STRIKETHROUGH_PATTERN.containsMatchIn(testPattern)
    }
    
    private fun validateHorizontalRules(testPattern: String): Boolean {
        val lines = testPattern.lines()
        return lines.any { MarkdownSyntax.HorizontalRules.isHorizontalRule(it) }
    }
    
    /**
     * 生成验证报告
     */
    fun generateValidationReport(): String {
        val result = validateBasicSyntax()
        
        return buildString {
            appendLine("=== Markdown 基础语法验证报告 ===")
            appendLine()
            appendLine("验证状态: ${if (result.isValid) "✅ 通过" else "❌ 失败"}")
            appendLine("支持特性数量: ${result.supportedFeatures.size}/${SyntaxFeature.values().size}")
            appendLine()
            
            if (result.supportedFeatures.isNotEmpty()) {
                appendLine("✅ 支持的特性:")
                result.supportedFeatures.forEach { feature ->
                    appendLine("  - $feature")
                }
                appendLine()
            }
            
            if (result.unsupportedFeatures.isNotEmpty()) {
                appendLine("❌ 不支持的特性:")
                result.unsupportedFeatures.forEach { feature ->
                    appendLine("  - $feature")
                }
                appendLine()
            }
            
            if (result.errors.isNotEmpty()) {
                appendLine("⚠️ 验证错误:")
                result.errors.forEach { error ->
                    appendLine("  - $error")
                }
            }
        }
    }
}