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
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow

/**
 * 现代化表格插件
 * 提供美观的表格样式和交互效果
 */
class ModernTablePlugin(private val config: MarkdownStyleConfigV2) : AbstractMarkwonPlugin() {
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configureTableSpans(builder)
    }
    
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        configureTableVisitor(builder)
    }
    
    private fun createModernTableTheme(): TableTheme {
        return TableTheme.Builder()
            .tableBorderColor(config.tableBorderColor)
            .tableBorderWidth(config.tableBorderWidth)
            .tableCellPadding(config.tableCellPadding)
            .tableHeaderRowBackgroundColor(config.tableHeaderBackgroundColor)
            .tableEvenRowBackgroundColor(config.tableAlternateRowColor)
            .tableOddRowBackgroundColor(config.surfaceColor)
            .build()
    }
    
    private fun configureTableSpans(builder: MarkwonSpansFactory.Builder) {
        // 表格容器
        builder.setFactory(TableBlock::class.java) { _, _ ->
            arrayOf(
                ModernTableContainerSpan(config)
            )
        }
        
        // 表头
        builder.setFactory(TableHead::class.java) { _, _ ->
            arrayOf(
                ModernTableHeaderSpan(config)
            )
        }
        
        // 表体
        builder.setFactory(TableBody::class.java) { _, _ ->
            arrayOf(
                ModernTableBodySpan(config)
            )
        }
        
        // 表格行 - 暂时注释掉，使用默认处理
        // builder.setFactory(TableRow::class.java) { _, props ->
        //     val isHeader = props.get("is_header") as? Boolean ?: false
        //     val rowIndex = props.get("row_index") as? Int ?: 0
        //     
        //     arrayOf(
        //         ModernTableRowSpan(config, isHeader, rowIndex)
        //     )
        // }
        
        // 表格单元格 - 暂时注释掉，使用默认处理
        // builder.setFactory(TableCell::class.java) { _, props ->
        //     val isHeader = props.get("is_header") as? Boolean ?: false
        //     val alignment = props.get("alignment") as? TableCell.Alignment ?: TableCell.Alignment.LEFT
        //     
        //     arrayOf(
        //         ModernTableCellSpan(config, isHeader, alignment)
        //     )
        // }
    }
    
    private fun configureTableVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(TableBlock::class.java) { visitor, tableBlock ->
            val length = visitor.length()
            visitor.visitChildren(tableBlock)
            
            // 为整个表格添加容器样式
            visitor.setSpansForNodeOptional(tableBlock, length)
        }
        
        builder.on(TableHead::class.java) { visitor, tableHead ->
            val length = visitor.length()
            visitor.visitChildren(tableHead)
            visitor.setSpansForNodeOptional(tableHead, length)
        }
        
        builder.on(TableBody::class.java) { visitor, tableBody ->
            val length = visitor.length()
            visitor.visitChildren(tableBody)
            visitor.setSpansForNodeOptional(tableBody, length)
        }
        
        builder.on(TableRow::class.java) { visitor, tableRow ->
            val length = visitor.length()
            
            // 判断是否为表头行
            val isHeader = tableRow.parent is TableHead
            // visitor.setSpanFlag("is_header", isHeader) // 暂时注释掉
            
            visitor.visitChildren(tableRow)
            visitor.setSpansForNodeOptional(tableRow, length)
        }
        
        builder.on(TableCell::class.java) { visitor, tableCell ->
            val length = visitor.length()
            
            // 获取对齐方式
            val alignment = tableCell.alignment
            // visitor.setSpanFlag("alignment", alignment) // 暂时注释掉
            
            // 判断是否为表头单元格
            val isHeader = tableCell.parent?.parent is TableHead
            // visitor.setSpanFlag("is_header", isHeader) // 暂时注释掉
            
            visitor.visitChildren(tableCell)
            visitor.setSpansForNodeOptional(tableCell, length)
        }
    }
}

/**
 * 现代化表格容器样式
 */
class ModernTableContainerSpan(private val config: MarkdownStyleConfigV2) : ReplacementSpan() {
    
    private val paint = Paint().apply {
        color = config.surfaceColor
        isAntiAlias = true
    }
    
    private val borderPaint = Paint().apply {
        color = config.tableBorderColor
        style = Paint.Style.STROKE
        strokeWidth = config.tableBorderWidth.toFloat()
        isAntiAlias = true
    }
    
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
        // 绘制表格背景和边框
        val rect = RectF(x, top.toFloat(), x + getSize(paint, text, start, end, null), bottom.toFloat())
        
        // 背景
        canvas.drawRoundRect(rect, config.tableCornerRadius, config.tableCornerRadius, this.paint)
        
        // 边框
        canvas.drawRoundRect(rect, config.tableCornerRadius, config.tableCornerRadius, borderPaint)
        
        // 绘制文本
        canvas.drawText(text!!, start, end, x, y.toFloat(), paint)
    }
}

/**
 * 现代化表头样式
 */
class ModernTableHeaderSpan(private val config: MarkdownStyleConfigV2) : BackgroundColorSpan(config.tableHeaderBackgroundColor) {
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        super.updateDrawState(ds)
        ds.color = config.tableHeaderTextColor
        ds.typeface = Typeface.DEFAULT_BOLD
    }
}

/**
 * 现代化表体样式
 */
class ModernTableBodySpan(private val config: MarkdownStyleConfigV2) : MetricAffectingSpan() {
    
    override fun updateMeasureState(textPaint: android.text.TextPaint) {
        textPaint.color = config.onSurfaceColor
    }
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        ds.color = config.onSurfaceColor
    }
}

/**
 * 现代化表格行样式
 */
class ModernTableRowSpan(
    private val config: MarkdownStyleConfigV2,
    private val isHeader: Boolean,
    private val rowIndex: Int
) : LineBackgroundSpan {
    
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
        val backgroundColor = when {
            isHeader -> config.tableHeaderBackgroundColor
            rowIndex % 2 == 0 -> config.surfaceColor
            else -> config.tableAlternateRowColor
        }
        
        val backgroundPaint = Paint().apply {
            color = backgroundColor
            isAntiAlias = true
        }
        
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), backgroundPaint)
        
        // 绘制行分割线
        if (!isHeader) {
            val dividerPaint = Paint().apply {
                color = config.tableBorderColor
                strokeWidth = 1f
                isAntiAlias = true
            }
            
            canvas.drawLine(
                left.toFloat(),
                bottom.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                dividerPaint
            )
        }
    }
}

/**
 * 现代化表格单元格样式
 */
class ModernTableCellSpan(
    private val config: MarkdownStyleConfigV2,
    private val isHeader: Boolean,
    private val alignment: TableCell.Alignment
) : MetricAffectingSpan(), LineHeightSpan {
    
    override fun updateMeasureState(textPaint: android.text.TextPaint) {
        textPaint.color = if (isHeader) config.tableHeaderTextColor else config.onSurfaceColor
        
        if (isHeader) {
            textPaint.typeface = Typeface.DEFAULT_BOLD
        }
    }
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        ds.color = if (isHeader) config.tableHeaderTextColor else config.onSurfaceColor
        
        if (isHeader) {
            ds.typeface = Typeface.DEFAULT_BOLD
        }
        
        // 设置文本对齐
        when (alignment) {
            TableCell.Alignment.LEFT -> ds.textAlign = Paint.Align.LEFT
            TableCell.Alignment.CENTER -> ds.textAlign = Paint.Align.CENTER
            TableCell.Alignment.RIGHT -> ds.textAlign = Paint.Align.RIGHT
        }
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
            val padding = config.tableCellPadding
            it.top -= padding / 2
            it.ascent -= padding / 4
            it.descent += padding / 4
            it.bottom += padding / 2
        }
    }
}

/**
 * 表格工具类
 */
object TableUtils {
    
    /**
     * 创建响应式表格
     * 在小屏幕上自动调整表格布局
     */
    fun createResponsiveTable(
        headers: List<String>,
        rows: List<List<String>>,
        config: MarkdownStyleConfigV2
    ): String {
        val builder = StringBuilder()
        
        // 表头
        builder.append("| ")
        headers.forEach { header ->
            builder.append("$header | ")
        }
        builder.append("\n")
        
        // 分割线
        builder.append("| ")
        headers.forEach { _ ->
            builder.append("--- | ")
        }
        builder.append("\n")
        
        // 数据行
        rows.forEach { row ->
            builder.append("| ")
            row.forEach { cell ->
                builder.append("$cell | ")
            }
            builder.append("\n")
        }
        
        return builder.toString()
    }
    
    /**
     * 创建带有排序功能的表格
     */
    fun createSortableTable(
        headers: List<String>,
        rows: List<List<String>>,
        sortableColumns: Set<Int> = emptySet()
    ): String {
        val builder = StringBuilder()
        
        // 表头（带排序指示器）
        builder.append("| ")
        headers.forEachIndexed { index, header ->
            val sortIndicator = if (index in sortableColumns) " ↕" else ""
            builder.append("$header$sortIndicator | ")
        }
        builder.append("\n")
        
        // 分割线
        builder.append("| ")
        headers.forEach { _ ->
            builder.append("--- | ")
        }
        builder.append("\n")
        
        // 数据行
        rows.forEach { row ->
            builder.append("| ")
            row.forEach { cell ->
                builder.append("$cell | ")
            }
            builder.append("\n")
        }
        
        return builder.toString()
    }
    
    /**
     * 创建带有筛选功能的表格
     */
    fun createFilterableTable(
        headers: List<String>,
        rows: List<List<String>>,
        filterableColumns: Set<Int> = emptySet()
    ): String {
        val builder = StringBuilder()
        
        // 筛选行（如果有可筛选列）
        if (filterableColumns.isNotEmpty()) {
            builder.append("| ")
            headers.forEachIndexed { index, _ ->
                val filter = if (index in filterableColumns) "🔍" else ""
                builder.append("$filter | ")
            }
            builder.append("\n")
        }
        
        // 表头
        builder.append("| ")
        headers.forEach { header ->
            builder.append("$header | ")
        }
        builder.append("\n")
        
        // 分割线
        builder.append("| ")
        headers.forEach { _ ->
            builder.append("--- | ")
        }
        builder.append("\n")
        
        // 数据行
        rows.forEach { row ->
            builder.append("| ")
            row.forEach { cell ->
                builder.append("$cell | ")
            }
            builder.append("\n")
        }
        
        return builder.toString()
    }
}