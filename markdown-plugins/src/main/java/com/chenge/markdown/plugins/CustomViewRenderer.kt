package com.chenge.markdown.plugins

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.BlockQuote
import java.util.regex.Pattern

/**
 * 自定义视图渲染器
 * 负责将Markdown表格和引用块渲染为自定义Android视图
 */
class CustomViewRenderer(private val context: Context) {
    
    private var config: MarkdownStyleConfigV2 = MarkdownStyleConfigV2()
    
    /**
     * 设置样式配置
     */
    fun setStyleConfig(config: MarkdownStyleConfigV2) {
        this.config = config
    }
    
    /**
     * 创建自定义视图插件
     */
    fun createCustomViewPlugin(): MarkwonPlugin {
        Log.d("CustomViewRenderer", "创建自定义视图插件")
        return object : AbstractMarkwonPlugin() {
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                Log.d("CustomViewRenderer", "配置自定义渲染逻辑")
                super.configureConfiguration(builder)
            }
            
            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                // 配置自定义表格渲染
                builder.setFactory(TableBlock::class.java) { _, _ ->
                    Log.d("CustomViewRenderer", "处理表格块")
                    arrayOf(CustomTableSpan(this@CustomViewRenderer))
                }
                
                // 配置自定义引用块渲染
                builder.setFactory(BlockQuote::class.java) { _, _ ->
                    Log.d("CustomViewRenderer", "处理引用块")
                    arrayOf(CustomQuoteSpan(this@CustomViewRenderer))
                }
            }
            
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                // 处理表格节点
                builder.on(TableBlock::class.java) { visitor, tableBlock ->
                    Log.d("CustomViewRenderer", "访问表格节点")
                    val length = visitor.length()
                    visitor.visitChildren(tableBlock)
                    visitor.setSpansForNodeOptional(tableBlock, length)
                }
                
                // 处理引用块节点
                builder.on(BlockQuote::class.java) { visitor, blockQuote ->
                    Log.d("CustomViewRenderer", "访问引用块节点")
                    val length = visitor.length()
                    visitor.visitChildren(blockQuote)
                    visitor.setSpansForNodeOptional(blockQuote, length)
                }
            }
        }
    }
    
    /**
     * 渲染表格到视图
     */
    fun renderTableToView(tableData: TableData): LinearLayout {
        Log.d("CustomViewRenderer", "开始渲染表格，行数: ${tableData.rows.size}, 列数: ${tableData.headers.size}")
        val tableLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // 设置表格背景
            background = createTableBackground()
            setPadding(
                config.tableCellPadding,
                config.tableCellPadding,
                config.tableCellPadding,
                config.tableCellPadding
            )
        }
        
        // 添加表头
        if (tableData.headers.isNotEmpty()) {
            val headerRow = createTableRow(tableData.headers, true)
            tableLayout.addView(headerRow)
        }
        
        // 添加数据行
        tableData.rows.forEach { rowData ->
            val dataRow = createTableRow(rowData, false)
            tableLayout.addView(dataRow)
        }
        
        return tableLayout
    }
    
    /**
     * 渲染引用块到视图
     */
    fun renderQuoteToView(quoteData: QuoteData): LinearLayout {
        val quoteLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            // 设置引用块背景
            background = createQuoteBackground(quoteData.type)
            setPadding(
                config.blockquoteMargin,
                config.blockquoteMargin,
                config.blockquoteMargin,
                config.blockquoteMargin
            )
        }
        
        // 添加左侧边框
        val borderView = createQuoteBorder(quoteData.type)
        quoteLayout.addView(borderView)
        
        // 添加内容
        val contentView = TextView(context).apply {
            text = quoteData.content
            setTextColor(config.blockquoteTextColor)
            textSize = config.textSize
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = config.blockquoteMargin
            }
        }
        
        quoteLayout.addView(contentView)
        
        return quoteLayout
    }
    
    /**
     * 从Markdown文本解析表格数据（支持嵌套表格）
     */
    fun parseTableFromMarkdown(markdown: String): List<TableData> {
        val tables = mutableListOf<TableData>()
        
        // 改进的表格匹配模式，支持嵌套在引用块中的表格
        val lines = markdown.split("\n")
        var i = 0
        
        while (i < lines.size) {
            val line = lines[i].trim()
            
            // 检查是否是表格行（可能在引用块中）
            val cleanLine = line.removePrefix(">").trim()
            
            if (cleanLine.startsWith("|") && cleanLine.endsWith("|")) {
                // 找到表格开始
                val tableLines = mutableListOf<String>()
                var j = i
                
                // 收集连续的表格行
                while (j < lines.size) {
                    val currentLine = lines[j].trim()
                    val currentCleanLine = currentLine.removePrefix(">").trim()
                    
                    if (currentCleanLine.startsWith("|") && currentCleanLine.endsWith("|")) {
                        tableLines.add(currentCleanLine)
                        j++
                    } else {
                        break
                    }
                }
                
                // 解析表格
                if (tableLines.size >= 2) {
                    val headerLine = tableLines[0]
                    val separatorLine = tableLines[1]
                    
                    // 检查分隔符行
                    if (separatorLine.contains("-")) {
                        val headers = headerLine.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                        val rows = mutableListOf<List<String>>()
                        
                        // 解析数据行（从第3行开始）
                        for (k in 2 until tableLines.size) {
                            val cells = tableLines[k].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                            if (cells.isNotEmpty()) {
                                rows.add(cells)
                            }
                        }
                        
                        if (headers.isNotEmpty()) {
                            tables.add(TableData(headers, rows))
                            Log.d("CustomViewRenderer", "解析到表格: 表头${headers.size}列, 数据${rows.size}行")
                        }
                    }
                }
                
                i = j
            } else {
                i++
            }
        }
        
        return tables
    }
    
    /**
     * 从Markdown文本解析引用块数据
     */
    fun parseQuotesFromMarkdown(markdown: String): List<QuoteData> {
        val quotes = mutableListOf<QuoteData>()
        val quotePattern = Pattern.compile("^>\\s*(.+)$", Pattern.MULTILINE)
        
        val matcher = quotePattern.matcher(markdown)
        while (matcher.find()) {
            val content = matcher.group(1)?.trim() ?: ""
            val type = detectQuoteType(content)
            quotes.add(QuoteData(content, type, 1))
        }
        
        return quotes
    }
    
    /**
     * 检测引用块类型
     */
    private fun detectQuoteType(content: String): QuoteType {
        return when {
            content.contains("[!NOTE]") || content.contains("💡") -> QuoteType.NOTE
            content.contains("[!TIP]") || content.contains("💡") -> QuoteType.TIP
            content.contains("[!WARNING]") || content.contains("⚠️") -> QuoteType.WARNING
            content.contains("[!DANGER]") || content.contains("🚨") -> QuoteType.DANGER
            content.contains("[!INFO]") || content.contains("ℹ️") -> QuoteType.INFO
            content.contains("[!SUCCESS]") || content.contains("✅") -> QuoteType.SUCCESS
            else -> QuoteType.DEFAULT
        }
    }
    
    /**
     * 创建表格背景
     */
    private fun createTableBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(config.tableHeaderBackgroundColor)
            cornerRadius = config.tableCornerRadius
            setStroke(config.tableBorderWidth, config.tableBorderColor)
        }
    }
    
    /**
     * 创建表格行
     */
    private fun createTableRow(cells: List<String>, isHeader: Boolean): LinearLayout {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        cells.forEach { cellText ->
            val cellView = TextView(context).apply {
                text = cellText
                setTextColor(if (isHeader) config.tableHeaderTextColor else config.textColor)
                textSize = config.textSize
                gravity = Gravity.CENTER
                setPadding(
                    config.tableCellPadding,
                    config.tableCellPadding,
                    config.tableCellPadding,
                    config.tableCellPadding
                )
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                
                // 设置单元格背景
                if (isHeader) {
                    setBackgroundColor(config.tableHeaderBackgroundColor)
                }
            }
            
            rowLayout.addView(cellView)
        }
        
        return rowLayout
    }
    
    /**
     * 创建引用块背景
     */
    private fun createQuoteBackground(type: QuoteType): GradientDrawable {
        return GradientDrawable().apply {
            when (type) {
                QuoteType.NOTE -> {
                    setColor(Color.parseColor("#E8F4FD")) // 浅蓝色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#1976D2"))
                }
                QuoteType.TIP -> {
                    setColor(Color.parseColor("#FFF3E0")) // 浅橙色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#FF9800"))
                }
                QuoteType.WARNING -> {
                    setColor(Color.parseColor("#FFF8E1")) // 浅黄色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#FFC107"))
                }
                QuoteType.DANGER -> {
                    setColor(Color.parseColor("#FFEBEE")) // 浅红色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#F44336"))
                }
                QuoteType.INFO -> {
                    setColor(Color.parseColor("#E3F2FD")) // 浅蓝色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#2196F3"))
                }
                QuoteType.SUCCESS -> {
                    setColor(Color.parseColor("#E8F5E8")) // 浅绿色
                    setStroke(config.blockquoteBorderWidth, Color.parseColor("#4CAF50"))
                }
                QuoteType.DEFAULT -> {
                    setColor(config.blockquoteBackgroundColor)
                    setStroke(config.blockquoteBorderWidth, config.blockquoteBorderColor)
                }
            }
            cornerRadius = config.blockquoteCornerRadius
        }
    }
    
    /**
     * 创建引用块边框
     */
    private fun createQuoteBorder(type: QuoteType): TextView {
        return TextView(context).apply {
            text = when (type) {
                QuoteType.NOTE -> "📝"
                QuoteType.TIP -> "💡"
                QuoteType.WARNING -> "⚠️"
                QuoteType.DANGER -> "🚨"
                QuoteType.INFO -> "ℹ️"
                QuoteType.SUCCESS -> "✅"
                QuoteType.DEFAULT -> "❝"
            }
            textSize = config.textSize * 0.8f
            setTextColor(config.blockquoteBorderColor)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }
    }
}

/**
 * 表格数据类
 */
data class TableData(
    val headers: List<String>,
    val rows: List<List<String>>
)

/**
 * 引用数据类
 */
data class QuoteData(
    val content: String,
    val type: QuoteType,
    val level: Int
)

/**
 * 自定义表格Span
 */
class CustomTableSpan(private val renderer: CustomViewRenderer) : ReplacementSpan() {
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // 返回表格的宽度（这里简化处理）
        return paint.measureText(text, start, end).toInt()
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
        // 这里可以绘制自定义表格
        // 为了简化，我们先绘制文本
        text?.let {
            canvas.drawText(it, start, end, x, y.toFloat(), paint)
        }
    }
}

/**
 * 自定义引用块Span
 */
class CustomQuoteSpan(private val renderer: CustomViewRenderer) : ReplacementSpan() {
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
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
        // 绘制自定义引用块
        text?.let {
            // 绘制左侧竖线
            val lineWidth = 4f
            val originalColor = paint.color
            paint.color = Color.BLUE
            canvas.drawRect(x, top.toFloat(), x + lineWidth, bottom.toFloat(), paint)
            
            // 恢复原色并绘制文本
            paint.color = originalColor
            canvas.drawText(it, start, end, x + lineWidth + 16f, y.toFloat(), paint)
        }
    }
}