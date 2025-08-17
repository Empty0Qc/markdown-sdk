package com.chenge.markdown.plugins

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.style.*
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.BlockQuote

/**
 * 现代化引用块插件
 * 提供美观的引用样式和多层级引用支持
 */
class ModernQuotePlugin(private val config: MarkdownStyleConfigV2) : AbstractMarkwonPlugin() {
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configureQuoteSpans(builder)
    }
    
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        configureQuoteVisitor(builder)
    }
    
    private fun configureQuoteSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(BlockQuote::class.java) { _, _ ->
            val level = 1 // 暂时使用默认值
            val quoteType = QuoteType.DEFAULT // 暂时使用默认值
            
            arrayOf(
                ModernQuoteSpan(config, level, quoteType),
                ModernQuoteBackgroundSpan(config, level, quoteType),
                ModernQuoteTextSpan(config, level, quoteType)
            )
        }
    }
    
    private fun configureQuoteVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(BlockQuote::class.java) { visitor, blockQuote ->
            val length = visitor.length()
            
            // 检测引用类型和层级
            val quoteInfo = detectQuoteInfo(blockQuote)
            // visitor.setSpanFlag("quote_level", quoteInfo.level) // 暂时注释掉
            // visitor.setSpanFlag("quote_type", quoteInfo.type) // 暂时注释掉
            
            // 添加引用前缀
            if (length == visitor.length()) {
                visitor.builder().append("\n")
            }
            
            visitor.visitChildren(blockQuote)
            
            // 添加引用后缀
            visitor.builder().append("\n")
            
            visitor.setSpansForNodeOptional(blockQuote, length)
        }
    }
    
    private fun detectQuoteInfo(blockQuote: BlockQuote): QuoteInfo {
        var level = 1
        var parent = blockQuote.parent
        
        // 计算嵌套层级
        while (parent != null) {
            if (parent is BlockQuote) {
                level++
            }
            parent = parent.parent
        }
        
        // 检测引用类型（基于内容）
        val content = extractTextContent(blockQuote).trim()
        val type = when {
            content.startsWith("[!NOTE]") || content.startsWith("💡") -> QuoteType.NOTE
            content.startsWith("[!TIP]") || content.startsWith("💡") -> QuoteType.TIP
            content.startsWith("[!WARNING]") || content.startsWith("⚠️") -> QuoteType.WARNING
            content.startsWith("[!DANGER]") || content.startsWith("🚨") -> QuoteType.DANGER
            content.startsWith("[!INFO]") || content.startsWith("ℹ️") -> QuoteType.INFO
            content.startsWith("[!SUCCESS]") || content.startsWith("✅") -> QuoteType.SUCCESS
            else -> QuoteType.DEFAULT
        }
        
        return QuoteInfo(level, type)
    }
    
    private fun extractTextContent(blockQuote: BlockQuote): String {
        val builder = StringBuilder()
        
        fun extractFromNode(node: org.commonmark.node.Node) {
            when (node) {
                is org.commonmark.node.Text -> builder.append(node.literal)
                is org.commonmark.node.Code -> builder.append(node.literal)
                else -> {
                    var child = node.firstChild
                    while (child != null) {
                        extractFromNode(child)
                        child = child.next
                    }
                }
            }
        }
        
        extractFromNode(blockQuote)
        return builder.toString()
    }
}

/**
 * 引用信息数据类
 */
data class QuoteInfo(
    val level: Int,
    val type: QuoteType
)

/**
 * 引用类型枚举
 */
enum class QuoteType {
    DEFAULT,
    NOTE,
    TIP,
    WARNING,
    DANGER,
    INFO,
    SUCCESS
}

/**
 * 现代化引用边框样式
 */
class ModernQuoteSpan(
    private val config: MarkdownStyleConfigV2,
    private val level: Int,
    private val quoteType: QuoteType
) : LeadingMarginSpan {
    
    private val borderPaint = Paint().apply {
        color = getBorderColor()
        strokeWidth = config.blockquoteBorderWidth.toFloat()
        isAntiAlias = true
    }
    
    private val iconPaint = Paint().apply {
        color = getBorderColor()
        textSize = config.textSize * 0.8f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    
    override fun getLeadingMargin(first: Boolean): Int {
        return config.blockquoteMargin + (level - 1) * config.blockquoteMargin
    }
    
    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        val margin = getLeadingMargin(first)
        val borderX = x + margin - config.blockquoteBorderWidth / 2
        
        // 绘制左侧边框
        canvas.drawLine(
            borderX.toFloat(),
            top.toFloat(),
            borderX.toFloat(),
            bottom.toFloat(),
            borderPaint
        )
        
        // 绘制类型图标（仅在第一行）
        if (first && quoteType != QuoteType.DEFAULT) {
            val icon = getQuoteIcon()
            val iconX = borderX + config.blockquoteBorderWidth + config.blockquotePadding
            val iconY = top + config.textSize
            
            canvas.drawText(icon, iconX.toFloat(), iconY, iconPaint)
        }
    }
    
    private fun getBorderColor(): Int {
        return when (quoteType) {
            QuoteType.NOTE -> config.blockquoteBorderColor
            QuoteType.TIP -> config.blockquoteBorderColor
            QuoteType.WARNING -> config.blockquoteBorderColor
            QuoteType.DANGER -> config.blockquoteBorderColor
            QuoteType.INFO -> config.blockquoteBorderColor
            QuoteType.SUCCESS -> config.blockquoteBorderColor
            QuoteType.DEFAULT -> config.blockquoteBorderColor
        }
    }
    
    private fun getQuoteIcon(): String {
        return when (quoteType) {
            QuoteType.NOTE -> "📝"
            QuoteType.TIP -> "💡"
            QuoteType.WARNING -> "⚠️"
            QuoteType.DANGER -> "🚨"
            QuoteType.INFO -> "ℹ️"
            QuoteType.SUCCESS -> "✅"
            QuoteType.DEFAULT -> "❝"
        }
    }
}

/**
 * 现代化引用背景样式
 */
class ModernQuoteBackgroundSpan(
    private val config: MarkdownStyleConfigV2,
    private val level: Int,
    private val quoteType: QuoteType
) : LineBackgroundSpan {
    
    private val backgroundPaint = Paint().apply {
        color = getBackgroundColor()
        isAntiAlias = true
    }
    
    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val margin = config.blockquoteMargin + (level - 1) * config.blockquoteMargin
        val backgroundLeft = left + margin
        val backgroundRight = right - config.blockquotePadding
        
        // 绘制背景
        val rect = RectF(
            backgroundLeft.toFloat(),
            top.toFloat(),
            backgroundRight.toFloat(),
            bottom.toFloat()
        )
        
        canvas.drawRoundRect(
            rect,
            config.blockquoteCornerRadius,
            config.blockquoteCornerRadius,
            backgroundPaint
        )
    }
    
    private fun getBackgroundColor(): Int {
        val alpha = 0x20 // 12.5% 透明度
        return when (quoteType) {
            QuoteType.NOTE -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.TIP -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.WARNING -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.DANGER -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.INFO -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.SUCCESS -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
            QuoteType.DEFAULT -> (config.blockquoteBackgroundColor and 0x00FFFFFF) or (alpha shl 24)
        }
    }
}

/**
 * 现代化引用文本样式
 */
class ModernQuoteTextSpan(
    private val config: MarkdownStyleConfigV2,
    private val level: Int,
    private val quoteType: QuoteType
) : MetricAffectingSpan(), LineHeightSpan {
    
    override fun updateMeasureState(textPaint: android.text.TextPaint) {
        updateTextPaint(textPaint)
    }
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        updateTextPaint(ds)
    }
    
    private fun updateTextPaint(textPaint: android.text.TextPaint) {
        // 设置文本颜色
        textPaint.color = getTextColor()
        
        // 设置字体样式
        when (quoteType) {
            QuoteType.NOTE, QuoteType.TIP -> {
                textPaint.typeface = Typeface.DEFAULT
            }
            QuoteType.WARNING, QuoteType.DANGER -> {
                textPaint.typeface = Typeface.DEFAULT_BOLD
            }
            else -> {
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
        }
        
        // 根据嵌套层级调整字体大小
        val sizeMultiplier = when (level) {
            1 -> 1.0f
            2 -> 0.95f
            3 -> 0.9f
            else -> 0.85f
        }
        textPaint.textSize = config.textSize * sizeMultiplier
    }
    
    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt?
    ) {
        fm?.let {
            val padding = config.blockquotePadding / 2f
            it.top -= padding.toInt()
            it.ascent -= (padding / 2f).toInt()
            it.descent += (padding / 2f).toInt()
            it.bottom += padding.toInt()
        }
    }
    
    private fun getTextColor(): Int {
        return when (quoteType) {
            QuoteType.NOTE -> config.blockquoteTextColor
            QuoteType.TIP -> config.blockquoteTextColor
            QuoteType.WARNING -> config.blockquoteTextColor
            QuoteType.DANGER -> config.blockquoteTextColor
            QuoteType.INFO -> config.blockquoteTextColor
            QuoteType.SUCCESS -> config.blockquoteTextColor
            QuoteType.DEFAULT -> config.blockquoteTextColor
        }
    }
}

/**
 * 引用工具类
 */
object QuoteUtils {
    
    /**
     * 创建带类型的引用块
     */
    fun createTypedQuote(type: QuoteType, content: String): String {
        val prefix = when (type) {
            QuoteType.NOTE -> "[!NOTE]"
            QuoteType.TIP -> "[!TIP]"
            QuoteType.WARNING -> "[!WARNING]"
            QuoteType.DANGER -> "[!DANGER]"
            QuoteType.INFO -> "[!INFO]"
            QuoteType.SUCCESS -> "[!SUCCESS]"
            QuoteType.DEFAULT -> ""
        }
        
        val lines = content.split("\n")
        val quotedLines = lines.map { line ->
            if (line == lines.first() && prefix.isNotEmpty()) {
                "> $prefix $line"
            } else {
                "> $line"
            }
        }
        
        return quotedLines.joinToString("\n")
    }
    
    /**
     * 创建多层级引用
     */
    fun createNestedQuote(levels: Int, content: String): String {
        val prefix = "> ".repeat(levels)
        val lines = content.split("\n")
        
        return lines.joinToString("\n") { line ->
            "$prefix$line"
        }
    }
    
    /**
     * 创建引用列表
     */
    fun createQuoteList(items: List<String>, type: QuoteType = QuoteType.DEFAULT): String {
        val builder = StringBuilder()
        
        val prefix = when (type) {
            QuoteType.NOTE -> "[!NOTE]"
            QuoteType.TIP -> "[!TIP]"
            QuoteType.WARNING -> "[!WARNING]"
            QuoteType.DANGER -> "[!DANGER]"
            QuoteType.INFO -> "[!INFO]"
            QuoteType.SUCCESS -> "[!SUCCESS]"
            QuoteType.DEFAULT -> ""
        }
        
        if (prefix.isNotEmpty()) {
            builder.append("> $prefix\n>\n")
        }
        
        items.forEachIndexed { index, item ->
            builder.append("> ${index + 1}. $item\n")
        }
        
        return builder.toString()
    }
    
    /**
     * 创建引用表格
     */
    fun createQuoteTable(
        headers: List<String>,
        rows: List<List<String>>,
        type: QuoteType = QuoteType.DEFAULT
    ): String {
        val builder = StringBuilder()
        
        val prefix = when (type) {
            QuoteType.NOTE -> "[!NOTE]"
            QuoteType.TIP -> "[!TIP]"
            QuoteType.WARNING -> "[!WARNING]"
            QuoteType.DANGER -> "[!DANGER]"
            QuoteType.INFO -> "[!INFO]"
            QuoteType.SUCCESS -> "[!SUCCESS]"
            QuoteType.DEFAULT -> ""
        }
        
        if (prefix.isNotEmpty()) {
            builder.append("> $prefix\n>\n")
        }
        
        // 表头
        builder.append("> | ")
        headers.forEach { header ->
            builder.append("$header | ")
        }
        builder.append("\n")
        
        // 分割线
        builder.append("> | ")
        headers.forEach { _ ->
            builder.append("--- | ")
        }
        builder.append("\n")
        
        // 数据行
        rows.forEach { row ->
            builder.append("> | ")
            row.forEach { cell ->
                builder.append("$cell | ")
            }
            builder.append("\n")
        }
        
        return builder.toString()
    }
    
    /**
     * 解析引用类型
     */
    fun parseQuoteType(content: String): QuoteType {
        val trimmed = content.trim()
        return when {
            trimmed.startsWith("[!NOTE]") -> QuoteType.NOTE
            trimmed.startsWith("[!TIP]") -> QuoteType.TIP
            trimmed.startsWith("[!WARNING]") -> QuoteType.WARNING
            trimmed.startsWith("[!DANGER]") -> QuoteType.DANGER
            trimmed.startsWith("[!INFO]") -> QuoteType.INFO
            trimmed.startsWith("[!SUCCESS]") -> QuoteType.SUCCESS
            else -> QuoteType.DEFAULT
        }
    }
    
    /**
     * 移除引用前缀
     */
    fun removeQuotePrefix(content: String): String {
        val lines = content.split("\n")
        return lines.joinToString("\n") { line ->
            line.replaceFirst(Regex("^>\\s*"), "")
        }
    }
    
    /**
     * 计算引用嵌套层级
     */
    fun calculateQuoteLevel(line: String): Int {
        var level = 0
        var index = 0
        
        while (index < line.length) {
            if (line[index] == '>') {
                level++
                index++
                // 跳过空格
                while (index < line.length && line[index] == ' ') {
                    index++
                }
            } else {
                break
            }
        }
        
        return level
    }
}