package com.chenge.markdown.core

/**
 * Markdown 基础语法元素定义
 * 支持 GFM (GitHub Flavored Markdown) 标准
 */
object MarkdownSyntax {
    
    /**
     * 标题语法 (H1-H6)
     */
    object Headers {
        val H1_PATTERN = Regex("^# (.+)$", RegexOption.MULTILINE)
        val H2_PATTERN = Regex("^## (.+)$", RegexOption.MULTILINE)
        val H3_PATTERN = Regex("^### (.+)$", RegexOption.MULTILINE)
        val H4_PATTERN = Regex("^#### (.+)$", RegexOption.MULTILINE)
        val H5_PATTERN = Regex("^##### (.+)$", RegexOption.MULTILINE)
        val H6_PATTERN = Regex("^###### (.+)$", RegexOption.MULTILINE)
        
        fun isHeader(line: String): Boolean {
            return line.trimStart().startsWith("#")
        }
        
        fun getHeaderLevel(line: String): Int {
            val trimmed = line.trimStart()
            var level = 0
            for (char in trimmed) {
                if (char == '#') level++
                else break
            }
            return if (level in 1..6) level else 0
        }
    }
    
    /**
     * 段落语法
     */
    object Paragraphs {
        fun isParagraph(line: String): Boolean {
            val trimmed = line.trim()
            return trimmed.isNotEmpty() && 
                   !Headers.isHeader(line) &&
                   !Lists.isList(line) &&
                   !CodeBlocks.isCodeBlock(line) &&
                   !Blockquotes.isBlockquote(line)
        }
    }
    
    /**
     * 列表语法 (有序和无序)
     */
    object Lists {
        val UNORDERED_PATTERN = Regex("^\\s*[-*+]\\s+(.+)$")
        val ORDERED_PATTERN = Regex("^\\s*\\d+\\.\\s+(.+)$")
        
        fun isList(line: String): Boolean {
            return UNORDERED_PATTERN.matches(line) || ORDERED_PATTERN.matches(line)
        }
        
        fun isUnorderedList(line: String): Boolean {
            return UNORDERED_PATTERN.matches(line)
        }
        
        fun isOrderedList(line: String): Boolean {
            return ORDERED_PATTERN.matches(line)
        }
        
        fun getListIndentLevel(line: String): Int {
            return line.takeWhile { it.isWhitespace() }.length / 2
        }
    }
    
    /**
     * 代码块语法
     */
    object CodeBlocks {
        val FENCED_CODE_PATTERN = Regex("^```([a-zA-Z0-9]*)?$")
        val INLINE_CODE_PATTERN = Regex("`([^`]+)`")
        
        fun isCodeBlock(line: String): Boolean {
            return line.trim().startsWith("```") || line.startsWith("    ")
        }
        
        fun isFencedCodeStart(line: String): Boolean {
            return FENCED_CODE_PATTERN.matches(line.trim())
        }
        
        fun isFencedCodeEnd(line: String): Boolean {
            return line.trim() == "```"
        }
        
        fun getCodeLanguage(line: String): String? {
            val match = FENCED_CODE_PATTERN.find(line.trim())
            return match?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
        }
    }
    
    /**
     * 引用块语法
     */
    object Blockquotes {
        val BLOCKQUOTE_PATTERN = Regex("^>\\s*(.*)$")
        
        fun isBlockquote(line: String): Boolean {
            return line.trimStart().startsWith(">")
        }
        
        fun getQuoteLevel(line: String): Int {
            val trimmed = line.trimStart()
            var level = 0
            for (char in trimmed) {
                if (char == '>') level++
                else if (char == ' ') continue
                else break
            }
            return level
        }
    }
    
    /**
     * 链接语法
     */
    object Links {
        val LINK_PATTERN = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val AUTO_LINK_PATTERN = Regex("<(https?://[^>]+)>")
        val REFERENCE_LINK_PATTERN = Regex("\\[([^\\]]+)\\]\\[([^\\]]+)\\]")
        
        fun hasLinks(text: String): Boolean {
            return LINK_PATTERN.containsMatchIn(text) || 
                   AUTO_LINK_PATTERN.containsMatchIn(text) ||
                   REFERENCE_LINK_PATTERN.containsMatchIn(text)
        }
    }
    
    /**
     * 图片语法
     */
    object Images {
        val IMAGE_PATTERN = Regex("!\\[([^\\]]*)\\]\\(([^)]+)\\)")
        val REFERENCE_IMAGE_PATTERN = Regex("!\\[([^\\]]*)\\]\\[([^\\]]+)\\]")
        
        fun hasImages(text: String): Boolean {
            return IMAGE_PATTERN.containsMatchIn(text) || 
                   REFERENCE_IMAGE_PATTERN.containsMatchIn(text)
        }
    }
    
    /**
     * 强调语法 (粗体、斜体)
     */
    object Emphasis {
        val BOLD_PATTERN = Regex("\\*\\*([^*]+)\\*\\*|__([^_]+)__")
        val ITALIC_PATTERN = Regex("\\*([^*]+)\\*|_([^_]+)_")
        val STRIKETHROUGH_PATTERN = Regex("~~([^~]+)~~")
        
        fun hasEmphasis(text: String): Boolean {
            return BOLD_PATTERN.containsMatchIn(text) ||
                   ITALIC_PATTERN.containsMatchIn(text) ||
                   STRIKETHROUGH_PATTERN.containsMatchIn(text)
        }
    }
    
    /**
     * 水平分割线语法
     */
    object HorizontalRules {
        val HR_PATTERN = Regex("^\\s*(-{3,}|\\*{3,}|_{3,})\\s*$")
        
        fun isHorizontalRule(line: String): Boolean {
            return HR_PATTERN.matches(line)
        }
    }
    
    /**
     * 检查文本是否包含任何 Markdown 语法
     */
    fun hasMarkdownSyntax(text: String): Boolean {
        return text.lines().any { line ->
            Headers.isHeader(line) ||
            Lists.isList(line) ||
            CodeBlocks.isCodeBlock(line) ||
            Blockquotes.isBlockquote(line) ||
            HorizontalRules.isHorizontalRule(line)
        } || Links.hasLinks(text) ||
            Images.hasImages(text) ||
            Emphasis.hasEmphasis(text) ||
            CodeBlocks.INLINE_CODE_PATTERN.containsMatchIn(text)
    }
}