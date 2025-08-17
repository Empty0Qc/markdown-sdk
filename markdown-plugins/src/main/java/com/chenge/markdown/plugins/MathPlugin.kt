package com.chenge.markdown.plugins

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import java.util.regex.Pattern

/**
 * 数学公式渲染插件
 * 支持LaTeX数学公式的渲染和展示
 */
class MathPlugin(private val config: MarkdownStyleConfigV2) : AbstractMarkwonPlugin() {
    
    companion object {
        // 行内数学公式模式：$...$
        private val INLINE_MATH_PATTERN = Pattern.compile("\\$([^\\$]+)\\$")
        
        // 块级数学公式模式：$$...$$
        private val BLOCK_MATH_PATTERN = Pattern.compile("\\$\\$([\\s\\S]+?)\\$\\$")
        
        // 常用数学符号映射
        val MATH_SYMBOLS = mapOf(
            "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ",
            "epsilon" to "ε", "zeta" to "ζ", "eta" to "η", "theta" to "θ",
            "iota" to "ι", "kappa" to "κ", "lambda" to "λ", "mu" to "μ",
            "nu" to "ν", "xi" to "ξ", "omicron" to "ο", "pi" to "π",
            "rho" to "ρ", "sigma" to "σ", "tau" to "τ", "upsilon" to "υ",
            "phi" to "φ", "chi" to "χ", "psi" to "ψ", "omega" to "ω",
            "Alpha" to "Α", "Beta" to "Β", "Gamma" to "Γ", "Delta" to "Δ",
            "Epsilon" to "Ε", "Zeta" to "Ζ", "Eta" to "Η", "Theta" to "Θ",
            "Iota" to "Ι", "Kappa" to "Κ", "Lambda" to "Λ", "Mu" to "Μ",
            "Nu" to "Ν", "Xi" to "Ξ", "Omicron" to "Ο", "Pi" to "Π",
            "Rho" to "Ρ", "Sigma" to "Σ", "Tau" to "Τ", "Upsilon" to "Υ",
            "Phi" to "Φ", "Chi" to "Χ", "Psi" to "Ψ", "Omega" to "Ω",
            "sum" to "∑", "prod" to "∏", "int" to "∫", "oint" to "∮",
            "infty" to "∞", "partial" to "∂", "nabla" to "∇", "pm" to "±",
            "mp" to "∓", "times" to "×", "div" to "÷", "cdot" to "·",
            "leq" to "≤", "geq" to "≥", "neq" to "≠", "approx" to "≈",
            "equiv" to "≡", "propto" to "∝", "in" to "∈", "notin" to "∉",
            "subset" to "⊂", "supset" to "⊃", "subseteq" to "⊆", "supseteq" to "⊇",
            "cap" to "∩", "cup" to "∪", "emptyset" to "∅", "forall" to "∀",
            "exists" to "∃", "neg" to "¬", "land" to "∧", "lor" to "∨",
            "rightarrow" to "→", "leftarrow" to "←", "leftrightarrow" to "↔",
            "Rightarrow" to "⇒", "Leftarrow" to "⇐", "Leftrightarrow" to "⇔"
        )
    }
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configureMathSpans(builder)
    }
    
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        configureMathVisitor(builder)
    }
    
    override fun processMarkdown(markdown: String): String {
        return processMathExpressions(markdown)
    }
    
    private fun configureMathSpans(builder: MarkwonSpansFactory.Builder) {
        // 为代码块添加数学公式检测
        builder.setFactory(FencedCodeBlock::class.java) { _, _ ->
            arrayOf(BlockMathSpan(config))
        }
        
        // 为行内代码添加数学公式检测
        builder.setFactory(Code::class.java) { _, _ ->
            arrayOf(InlineMathSpan(config, ""))
        }
    }
    
    private fun configureMathVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(FencedCodeBlock::class.java) { visitor, fencedCodeBlock ->
            val info = fencedCodeBlock.info
            if (info == "math" || info == "latex") {
                val length = visitor.length()
                visitor.builder().append(fencedCodeBlock.literal)
                visitor.setSpansForNodeOptional(fencedCodeBlock, length)
            } else {
                // 使用默认处理
                visitor.visitChildren(fencedCodeBlock)
            }
        }
        
        builder.on(Code::class.java) { visitor, code ->
            val literal = code.literal
            if (literal.startsWith("$") && literal.endsWith("$")) {
                val length = visitor.length()
                visitor.builder().append(literal)
                visitor.setSpansForNodeOptional(code, length)
            } else {
                // 使用默认处理
                visitor.visitChildren(code)
            }
        }
    }
    
    private fun processMathExpressions(markdown: String): String {
        var processed = markdown
        
        // 处理块级数学公式
        val blockMatcher = BLOCK_MATH_PATTERN.matcher(processed)
        val blockBuffer = StringBuffer()
        while (blockMatcher.find()) {
            val mathContent = blockMatcher.group(1)
            blockMatcher.appendReplacement(blockBuffer, "```math\n$mathContent\n```")
        }
        blockMatcher.appendTail(blockBuffer)
        processed = blockBuffer.toString()
        
        // 处理行内数学公式
        val inlineMatcher = INLINE_MATH_PATTERN.matcher(processed)
        val inlineBuffer = StringBuffer()
        while (inlineMatcher.find()) {
            val mathContent = inlineMatcher.group(1)
            inlineMatcher.appendReplacement(inlineBuffer, "`\$$mathContent\$`")
        }
        inlineMatcher.appendTail(inlineBuffer)
        processed = inlineBuffer.toString()
        
        return processed
    }
}

/**
 * 行内数学公式样式
 */
class InlineMathSpan(
    private val config: MarkdownStyleConfigV2,
    private val mathExpression: String
) : ReplacementSpan() {
    
    private val paint = Paint().apply {
        color = config.codeTextColor
        textSize = config.textSize * 0.9f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = config.codeBackgroundColor
        isAntiAlias = true
    }
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val renderedText = renderSimpleMath(mathExpression)
        return this.paint.measureText(renderedText).toInt() + config.codePadding * 2
    }
    
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val renderedText = renderSimpleMath(mathExpression)
        val width = getSize(paint, text, start, end, null)
        val height = bottom - top
        
        // 绘制背景
        val rect = RectF(
            x,
            top.toFloat(),
            x + width,
            bottom.toFloat()
        )
        canvas.drawRoundRect(rect, config.codeCornerRadius, config.codeCornerRadius, backgroundPaint)
        
        // 绘制数学公式
        val textX = x + config.codePadding
        val textY = y.toFloat()
        canvas.drawText(renderedText, textX, textY, this.paint)
    }
    
    private fun renderSimpleMath(expression: String): String {
        var rendered = expression
        
        // 替换常用数学符号
        for ((latex, unicode) in MathPlugin.MATH_SYMBOLS) {
            rendered = rendered.replace("\\$latex", unicode)
        }
        
        // 处理上标和下标的简单情况
        rendered = rendered.replace(Regex("\\^\\{([^}]+)\\}")) { matchResult ->
            val superscript = matchResult.groupValues[1]
            convertToSuperscript(superscript)
        }
        
        rendered = rendered.replace(Regex("_\\{([^}]+)\\}")) { matchResult ->
            val subscript = matchResult.groupValues[1]
            convertToSubscript(subscript)
        }
        
        // 处理分数的简单情况
        rendered = rendered.replace(Regex("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}")) { matchResult ->
            val numerator = matchResult.groupValues[1]
            val denominator = matchResult.groupValues[2]
            "$numerator/$denominator"
        }
        
        return rendered
    }
    
    private fun convertToSuperscript(text: String): String {
        val superscriptMap = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
            'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
            'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ',
            'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ',
            'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
            'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ', 'z' to 'ᶻ'
        )
        
        return text.map { char ->
            superscriptMap[char] ?: char
        }.joinToString("")
    }
    
    private fun convertToSubscript(text: String): String {
        val subscriptMap = mapOf(
            '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
            '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
            '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
            'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
            'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
            'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
            'v' to 'ᵥ', 'x' to 'ₓ'
        )
        
        return text.map { char ->
            subscriptMap[char] ?: char
        }.joinToString("")
    }
}

/**
 * 块级数学公式样式
 */
class BlockMathSpan(private val config: MarkdownStyleConfigV2) : ReplacementSpan() {
    
    private val paint = Paint().apply {
        color = config.codeBlockTextColor
        textSize = config.textSize
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint().apply {
        color = config.codeBlockBackgroundColor
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = config.codeBlockBorderColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // 块级公式占满宽度
        return Int.MAX_VALUE
    }
    
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val mathExpression = text?.subSequence(start, end)?.toString() ?: ""
        val renderedText = renderComplexMath(mathExpression)
        
        val width = canvas.width.toFloat()
        val height = (bottom - top).toFloat()
        
        // 绘制背景
        val rect = RectF(0f, top.toFloat(), width, bottom.toFloat())
        canvas.drawRoundRect(rect, config.codeBlockCornerRadius, config.codeBlockCornerRadius, backgroundPaint)
        
        // 绘制边框
        canvas.drawRoundRect(rect, config.codeBlockCornerRadius, config.codeBlockCornerRadius, borderPaint)
        
        // 绘制数学公式（居中）
        val textX = width / 2
        val textY = top + height / 2 + paint.textSize / 3
        canvas.drawText(renderedText, textX, textY, this.paint)
    }
    
    private fun renderComplexMath(expression: String): String {
        var rendered = expression.trim()
        
        // 替换常用数学符号
        MathPlugin.MATH_SYMBOLS.forEach { (latex, unicode) ->
            rendered = rendered.replace("\\$latex", unicode)
        }
        
        // 处理矩阵
        rendered = rendered.replace(Regex("\\\\begin\\{matrix\\}([\\s\\S]*?)\\\\end\\{matrix\\}")) { matchResult ->
            val matrixContent = matchResult.groupValues[1]
            formatMatrix(matrixContent)
        }
        
        // 处理求和符号
        rendered = rendered.replace(Regex("\\\\sum_\\{([^}]+)\\}\\^\\{([^}]+)\\}")) { matchResult ->
            val lower = matchResult.groupValues[1]
            val upper = matchResult.groupValues[2]
            "∑($lower→$upper)"
        }
        
        // 处理积分符号
        rendered = rendered.replace(Regex("\\\\int_\\{([^}]+)\\}\\^\\{([^}]+)\\}")) { matchResult ->
            val lower = matchResult.groupValues[1]
            val upper = matchResult.groupValues[2]
            "∫($lower→$upper)"
        }
        
        // 处理极限
        rendered = rendered.replace(Regex("\\\\lim_\\{([^}]+)\\}")) { matchResult ->
            val limit = matchResult.groupValues[1]
            "lim($limit)"
        }
        
        return rendered
    }
    
    private fun formatMatrix(content: String): String {
        val rows = content.split("\\\\\\\\")
        val formattedRows = rows.map { row ->
            row.split("&").joinToString(" ")
        }
        return "[${formattedRows.joinToString("; ")}]"
    }
}

/**
 * 数学公式工具类
 */
object MathUtils {
    
    /**
     * 验证LaTeX数学公式的语法
     */
    fun validateMathExpression(expression: String): Boolean {
        // 检查括号匹配
        val brackets = listOf('(' to ')', '[' to ']', '{' to '}')
        val stack = mutableListOf<Char>()
        
        for (char in expression) {
            when {
                brackets.any { it.first == char } -> stack.add(char)
                brackets.any { it.second == char } -> {
                    if (stack.isEmpty()) return false
                    val lastOpen = stack.removeAt(stack.size - 1)
                    val expectedClose = brackets.find { it.first == lastOpen }?.second
                    if (expectedClose != char) return false
                }
            }
        }
        
        return stack.isEmpty()
    }
    
    /**
     * 提取数学公式中的变量
     */
    fun extractVariables(expression: String): Set<String> {
        val variables = mutableSetOf<String>()
        val pattern = Pattern.compile("[a-zA-Z]+")
        val matcher = pattern.matcher(expression)
        
        while (matcher.find()) {
            val variable = matcher.group()
            // 排除LaTeX命令
            if (!variable.startsWith("\\")) {
                variables.add(variable)
            }
        }
        
        return variables
    }
    
    /**
     * 简化数学表达式
     */
    fun simplifyExpression(expression: String): String {
        var simplified = expression
        
        // 移除多余的空格
        simplified = simplified.replace(Regex("\\s+"), " ").trim()
        
        // 简化连续的运算符
        simplified = simplified.replace("+-", "-")
        simplified = simplified.replace("-+", "-")
        simplified = simplified.replace("++", "+")
        simplified = simplified.replace("--", "+")
        
        return simplified
    }
    
    /**
     * 格式化数学公式以便显示
     */
    fun formatForDisplay(expression: String): String {
        var formatted = expression
        
        // 在运算符前后添加空格
        formatted = formatted.replace(Regex("([+\\-*/=])"), " $1 ")
        
        // 移除多余的空格
        formatted = formatted.replace(Regex("\\s+"), " ").trim()
        
        return formatted
    }
}