package com.chenge.markdown.plugins

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import java.util.regex.Pattern

/**
 * 代码语法高亮插件
 * 支持多种编程语言的语法高亮显示
 */
class SyntaxHighlightPlugin(private val config: MarkdownStyleConfigV2) : AbstractMarkwonPlugin() {
    
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(FencedCodeBlock::class.java) { visitor, fencedCodeBlock ->
            val language = fencedCodeBlock.info?.trim()?.lowercase() ?: ""
            val code = fencedCodeBlock.literal
            
            if (config.enableSyntaxHighlighting && code != null) {
                val highlighted = highlightCode(code, language)
                visitor.builder().append(highlighted)
            } else {
                visitor.visitChildren(fencedCodeBlock)
            }
        }
        
        builder.on(IndentedCodeBlock::class.java) { visitor, indentedCodeBlock ->
            val code = indentedCodeBlock.literal
            
            if (config.enableSyntaxHighlighting && code != null) {
                val highlighted = highlightCode(code, "")
                visitor.builder().append(highlighted)
            } else {
                visitor.visitChildren(indentedCodeBlock)
            }
        }
    }
    
    private fun highlightCode(code: String, language: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(code)
        
        when (language) {
            "java", "kotlin" -> highlightJavaKotlin(spannable)
            "javascript", "js", "typescript", "ts" -> highlightJavaScript(spannable)
            "python", "py" -> highlightPython(spannable)
            "xml", "html" -> highlightXmlHtml(spannable)
            "json" -> highlightJson(spannable)
            "css" -> highlightCss(spannable)
            "sql" -> highlightSql(spannable)
            "bash", "shell", "sh" -> highlightBash(spannable)
            else -> highlightGeneric(spannable)
        }
        
        return spannable
    }
    
    private fun highlightJavaKotlin(spannable: SpannableStringBuilder) {
        // 关键字
        val keywords = arrayOf(
            "abstract", "class", "interface", "enum", "object", "data", "sealed",
            "fun", "val", "var", "const", "lateinit", "by", "lazy",
            "public", "private", "protected", "internal", "open", "final", "override",
            "if", "else", "when", "for", "while", "do", "break", "continue", "return",
            "try", "catch", "finally", "throw", "throws",
            "import", "package", "as", "typealias",
            "true", "false", "null", "this", "super", "it",
            "in", "out", "is", "!is", "as?", "!!",
            "suspend", "inline", "noinline", "crossinline", "reified"
        )
        
        val colors = getSyntaxColors()
        highlightKeywords(spannable, keywords, colors.KEYWORD)
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT)
        highlightNumbers(spannable, colors.NUMBER)
        highlightAnnotations(spannable, colors.ANNOTATION)
    }
    
    private fun highlightJavaScript(spannable: SpannableStringBuilder) {
        val keywords = arrayOf(
            "var", "let", "const", "function", "return", "if", "else", "for", "while", "do",
            "break", "continue", "switch", "case", "default", "try", "catch", "finally",
            "throw", "new", "this", "typeof", "instanceof", "in", "of",
            "true", "false", "null", "undefined", "class", "extends", "super",
            "import", "export", "from", "as", "async", "await"
        )
        
        val colors = getSyntaxColors()
        highlightKeywords(spannable, keywords, colors.KEYWORD)
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT)
        highlightNumbers(spannable, colors.NUMBER)
    }
    
    private fun highlightPython(spannable: SpannableStringBuilder) {
        val keywords = arrayOf(
            "def", "class", "if", "elif", "else", "for", "while", "break", "continue",
            "return", "yield", "try", "except", "finally", "raise", "with", "as",
            "import", "from", "global", "nonlocal", "lambda", "pass", "del",
            "True", "False", "None", "and", "or", "not", "in", "is",
            "async", "await", "self", "cls"
        )
        
        val colors = getSyntaxColors()
        highlightKeywords(spannable, keywords, colors.KEYWORD)
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT, "#")
        highlightNumbers(spannable, colors.NUMBER)
        highlightDecorators(spannable, colors.ANNOTATION)
    }
    
    private fun highlightXmlHtml(spannable: SpannableStringBuilder) {
        val colors = getSyntaxColors()
        // XML/HTML 标签
        val tagPattern = Pattern.compile("</?\\w+[^>]*>")
        highlightPattern(spannable, tagPattern, colors.TAG)
        
        // 属性
        val attrPattern = Pattern.compile("\\w+=")
        highlightPattern(spannable, attrPattern, colors.ATTRIBUTE)
        
        // 字符串值
        highlightStrings(spannable, colors.STRING)
        
        // 注释
        val commentPattern = Pattern.compile("<!--[\\s\\S]*?-->")
        highlightPattern(spannable, commentPattern, colors.COMMENT)
    }
    
    private fun highlightJson(spannable: SpannableStringBuilder) {
        val colors = getSyntaxColors()
        // JSON 键
        val keyPattern = Pattern.compile("\"[^\"]*\"\\s*:")
        highlightPattern(spannable, keyPattern, colors.PROPERTY)
        
        // 字符串值
        highlightStrings(spannable, colors.STRING)
        
        // 数字
        highlightNumbers(spannable, colors.NUMBER)
        
        // 布尔值和null
        val boolNullPattern = Pattern.compile("\\b(true|false|null)\\b")
        highlightPattern(spannable, boolNullPattern, colors.KEYWORD)
    }
    
    private fun highlightCss(spannable: SpannableStringBuilder) {
        val colors = getSyntaxColors()
        // CSS 选择器
        val selectorPattern = Pattern.compile("[.#]?\\w+(?:\\s*[>+~]\\s*\\w+)*\\s*\\{")
        highlightPattern(spannable, selectorPattern, colors.SELECTOR)
        
        // CSS 属性
        val propertyPattern = Pattern.compile("\\w+(-\\w+)*\\s*:")
        highlightPattern(spannable, propertyPattern, colors.PROPERTY)
        
        // CSS 值
        highlightStrings(spannable, colors.STRING)
        highlightNumbers(spannable, colors.NUMBER)
        
        // 注释
        val commentPattern = Pattern.compile("/\\*[\\s\\S]*?\\*/")
        highlightPattern(spannable, commentPattern, colors.COMMENT)
    }
    
    private fun highlightSql(spannable: SpannableStringBuilder) {
        val keywords = arrayOf(
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
            "ALTER", "TABLE", "INDEX", "VIEW", "DATABASE", "SCHEMA",
            "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "OUTER", "ON", "USING",
            "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "OFFSET",
            "AND", "OR", "NOT", "IN", "EXISTS", "BETWEEN", "LIKE", "IS", "NULL",
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK",
            "INT", "VARCHAR", "TEXT", "DATE", "TIMESTAMP", "BOOLEAN"
        )
        
        val colors = getSyntaxColors()
        highlightKeywords(spannable, keywords, colors.KEYWORD)
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT, "--")
        highlightNumbers(spannable, colors.NUMBER)
    }
    
    private fun highlightBash(spannable: SpannableStringBuilder) {
        val keywords = arrayOf(
            "if", "then", "else", "elif", "fi", "for", "while", "do", "done",
            "case", "esac", "function", "return", "exit", "break", "continue",
            "echo", "printf", "read", "cd", "ls", "pwd", "mkdir", "rm", "cp", "mv",
            "grep", "sed", "awk", "sort", "uniq", "head", "tail", "cat", "less", "more"
        )
        
        val colors = getSyntaxColors()
        highlightKeywords(spannable, keywords, colors.KEYWORD)
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT, "#")
        
        // 变量
        val varPattern = Pattern.compile("\\$\\w+|\\$\\{[^}]+\\}")
        highlightPattern(spannable, varPattern, colors.VARIABLE)
    }
    
    private fun highlightGeneric(spannable: SpannableStringBuilder) {
        val colors = getSyntaxColors()
        highlightStrings(spannable, colors.STRING)
        highlightComments(spannable, colors.COMMENT)
        highlightNumbers(spannable, colors.NUMBER)
    }
    
    private fun highlightKeywords(spannable: SpannableStringBuilder, keywords: Array<String>, color: Int) {
        for (keyword in keywords) {
            val pattern = Pattern.compile("\\b$keyword\\b")
            highlightPattern(spannable, pattern, color, Typeface.BOLD)
        }
    }
    
    private fun highlightStrings(spannable: SpannableStringBuilder, color: Int) {
        // 双引号字符串
        val doubleQuotePattern = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"")
        highlightPattern(spannable, doubleQuotePattern, color)
        
        // 单引号字符串
        val singleQuotePattern = Pattern.compile("'(?:[^'\\\\]|\\\\.)*'")
        highlightPattern(spannable, singleQuotePattern, color)
        
        // 反引号字符串（模板字符串）
        val templatePattern = Pattern.compile("`(?:[^`\\\\]|\\\\.)*`")
        highlightPattern(spannable, templatePattern, color)
    }
    
    private fun highlightComments(spannable: SpannableStringBuilder, color: Int, prefix: String = "//") {
        val pattern = when (prefix) {
            "//" -> Pattern.compile("//.*$", Pattern.MULTILINE)
            "#" -> Pattern.compile("#.*$", Pattern.MULTILINE)
            "--" -> Pattern.compile("--.*$", Pattern.MULTILINE)
            else -> Pattern.compile("$prefix.*$", Pattern.MULTILINE)
        }
        highlightPattern(spannable, pattern, color, Typeface.ITALIC)
        
        // 多行注释
        if (prefix == "//") {
            val multiLinePattern = Pattern.compile("/\\*[\\s\\S]*?\\*/")
            highlightPattern(spannable, multiLinePattern, color, Typeface.ITALIC)
        }
    }
    
    private fun highlightNumbers(spannable: SpannableStringBuilder, color: Int) {
        val pattern = Pattern.compile("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?[fFdDlL]?\\b")
        highlightPattern(spannable, pattern, color)
    }
    
    private fun highlightAnnotations(spannable: SpannableStringBuilder, color: Int) {
        val pattern = Pattern.compile("@\\w+")
        highlightPattern(spannable, pattern, color, Typeface.BOLD)
    }
    
    private fun highlightDecorators(spannable: SpannableStringBuilder, color: Int) {
        val pattern = Pattern.compile("@\\w+")
        highlightPattern(spannable, pattern, color, Typeface.BOLD)
    }
    
    private fun highlightPattern(
        spannable: SpannableStringBuilder,
        pattern: Pattern,
        color: Int,
        style: Int? = null
    ) {
        val matcher = pattern.matcher(spannable)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            
            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            style?.let {
                spannable.setSpan(
                    StyleSpan(it),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
    
    /**
     * 语法高亮颜色定义
     */
    private fun getSyntaxColors() = object {
        val KEYWORD = if (config.isDarkTheme) Color.parseColor("#569CD6") else Color.parseColor("#0000FF")
        val STRING = if (config.isDarkTheme) Color.parseColor("#CE9178") else Color.parseColor("#008000")
        val COMMENT = if (config.isDarkTheme) Color.parseColor("#6A9955") else Color.parseColor("#808080")
        val NUMBER = if (config.isDarkTheme) Color.parseColor("#B5CEA8") else Color.parseColor("#FF0000")
        val ANNOTATION = if (config.isDarkTheme) Color.parseColor("#DCDCAA") else Color.parseColor("#808000")
        val TAG = if (config.isDarkTheme) Color.parseColor("#569CD6") else Color.parseColor("#800080")
        val ATTRIBUTE = if (config.isDarkTheme) Color.parseColor("#9CDCFE") else Color.parseColor("#FF0000")
        val PROPERTY = if (config.isDarkTheme) Color.parseColor("#9CDCFE") else Color.parseColor("#0000FF")
        val SELECTOR = if (config.isDarkTheme) Color.parseColor("#D7BA7D") else Color.parseColor("#800000")
        val VARIABLE = if (config.isDarkTheme) Color.parseColor("#4FC1FF") else Color.parseColor("#008080")
    }
}