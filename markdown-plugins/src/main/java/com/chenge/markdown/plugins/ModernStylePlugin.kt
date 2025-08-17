package com.chenge.markdown.plugins

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Layout
import android.text.Spanned
import android.text.style.*
import androidx.annotation.RequiresApi
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.spans.BlockQuoteSpan
import io.noties.markwon.core.spans.CodeSpan
import io.noties.markwon.core.spans.HeadingSpan
import org.commonmark.node.*

/**
 * 现代化样式插件，基于Material Design 3设计规范
 * 提供丰富的视觉效果和现代化的UI体验
 */
class ModernStylePlugin(private val config: MarkdownStyleConfigV2) : AbstractMarkwonPlugin() {
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configureHeadingSpans(builder)
        configureParagraphSpans(builder)
        configureCodeSpans(builder)
        configureLinkSpans(builder)
        configureBlockquoteSpans(builder)
        configureListSpans(builder)
        configureThematicBreakSpans(builder)
    }
    
    private fun configureHeadingSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Heading::class.java) { _, _ ->
            val level = 1 // 暂时使用默认值
            val scale = when (level) {
                1 -> config.headingScaleH1
                2 -> config.headingScaleH2
                3 -> config.headingScaleH3
                4 -> config.headingScaleH4
                5 -> config.headingScaleH5
                6 -> config.headingScaleH6
                else -> 1.0f
            }
            
            val spans = mutableListOf<Any>()
            
            // 字体大小
            spans.add(RelativeSizeSpan(scale))
            
            // 字体颜色
            spans.add(ForegroundColorSpan(config.headingColor))
            
            // 字体样式
            spans.add(StyleSpan(Typeface.BOLD))
            
            // 自定义字体
            config.headingTypeface?.let { typeface ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    spans.add(TypefaceSpan(typeface))
                }
            }
            
            // 标题间距
            spans.add(ModernHeadingSpan(config, level))
            
            spans.toTypedArray()
        }
    }
    
    private fun configureParagraphSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Paragraph::class.java) { _, _ ->
            arrayOf(
                ForegroundColorSpan(config.textColor),
                ModernParagraphSpan(config)
            )
        }
    }
    
    private fun configureCodeSpans(builder: MarkwonSpansFactory.Builder) {
        // 行内代码
        builder.setFactory(Code::class.java) { _, _ ->
            arrayOf(
                ModernCodeSpan(config)
            )
        }
        
        // 代码块
        builder.setFactory(FencedCodeBlock::class.java) { _, _ ->
            arrayOf(
                ModernCodeBlockSpan(config)
            )
        }
        
        builder.setFactory(IndentedCodeBlock::class.java) { _, _ ->
            arrayOf(
                ModernCodeBlockSpan(config)
            )
        }
    }
    
    private fun configureLinkSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Link::class.java) { _, _ ->
            val spans = mutableListOf<Any>()
            spans.add(ForegroundColorSpan(config.linkColor))
            
            if (config.linkUnderline) {
                spans.add(UnderlineSpan())
            }
            
            spans.add(ModernLinkSpan(config))
            
            spans.toTypedArray()
        }
    }
    
    private fun configureBlockquoteSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(BlockQuote::class.java) { _, _ ->
            arrayOf(
                ModernBlockquoteSpan(config)
            )
        }
    }
    
    private fun configureListSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(BulletList::class.java) { _, _ ->
            arrayOf(
                ModernBulletSpan(config)
            )
        }
        
        builder.setFactory(OrderedList::class.java) { _, _ ->
            arrayOf(
                ModernOrderedListSpan(config)
            )
        }
    }
    
    private fun configureThematicBreakSpans(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(ThematicBreak::class.java) { _, _ ->
            arrayOf(
                ModernDividerSpan(config)
            )
        }
    }
}

/**
 * 现代化标题样式
 */
class ModernHeadingSpan(
    private val config: MarkdownStyleConfigV2,
    private val level: Int
) : LineHeightSpan {
    
    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt?
    ) {
        fm?.let {
            val extra = (config.headingMarginTop + config.headingMarginBottom) / 2
            it.top -= extra
            it.ascent -= extra / 2
            it.descent += extra / 2
            it.bottom += extra
        }
    }
}

/**
 * 现代化段落样式
 */
class ModernParagraphSpan(private val config: MarkdownStyleConfigV2) : LineHeightSpan {
    
    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt?
    ) {
        fm?.let {
            val lineHeight = (config.textSize * config.lineHeight).toInt()
            val extra = lineHeight - (it.descent - it.ascent)
            if (extra > 0) {
                it.top -= extra / 2
                it.ascent -= extra / 4
                it.descent += extra / 4
                it.bottom += extra / 2
            }
        }
    }
}

/**
 * 现代化行内代码样式
 */
class ModernCodeSpan(private val config: MarkdownStyleConfigV2) : ReplacementSpan() {
    
    private val paint = Paint().apply {
        color = config.codeBackgroundColor
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = config.codeTextColor
        typeface = config.codeTypeface
        isAntiAlias = true
    }
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        textPaint.textSize = paint.textSize
        val padding = config.codePadding * 2
        return (textPaint.measureText(text, start, end) + padding).toInt()
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
        textPaint.textSize = paint.textSize
        val padding = config.codePadding.toFloat()
        val width = textPaint.measureText(text, start, end) + padding * 2
        
        // 绘制圆角背景
        val rect = RectF(x, top.toFloat(), x + width, bottom.toFloat())
        canvas.drawRoundRect(rect, config.codeCornerRadius, config.codeCornerRadius, this.paint)
        
        // 绘制文本
        canvas.drawText(text!!, start, end, x + padding, y.toFloat(), textPaint)
    }
}

/**
 * 现代化代码块样式
 */
class ModernCodeBlockSpan(private val config: MarkdownStyleConfigV2) : LeadingMarginSpan {
    
    private val paint = Paint().apply {
        color = config.codeBlockBackgroundColor
        isAntiAlias = true
    }
    
    private val borderPaint = Paint().apply {
        color = config.codeBlockBorderColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    
    override fun getLeadingMargin(first: Boolean): Int {
        return config.codeBlockMargin + config.codeBlockPadding
    }
    
    override fun drawLeadingMargin(
        canvas: Canvas?,
        paint: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        canvas?.let { c ->
            val margin = config.codeBlockMargin.toFloat()
            val padding = config.codeBlockPadding.toFloat()
            
            // 绘制背景
            val rect = RectF(
                x.toFloat() - margin,
                top.toFloat() - padding,
                x.toFloat() + (layout?.width ?: 0) + margin,
                bottom.toFloat() + padding
            )
            c.drawRoundRect(rect, config.codeBlockCornerRadius, config.codeBlockCornerRadius, this.paint)
            
            // 绘制边框
            c.drawRoundRect(rect, config.codeBlockCornerRadius, config.codeBlockCornerRadius, borderPaint)
        }
    }
}

/**
 * 现代化链接样式
 */
class ModernLinkSpan(private val config: MarkdownStyleConfigV2) : ClickableSpan() {
    
    override fun onClick(widget: android.view.View) {
        // 链接点击处理将在后续实现
    }
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        super.updateDrawState(ds)
        ds.color = config.linkColor
        ds.isUnderlineText = config.linkUnderline
    }
}

/**
 * 现代化引用块样式
 */
class ModernBlockquoteSpan(private val config: MarkdownStyleConfigV2) : LeadingMarginSpan {
    
    private val paint = Paint().apply {
        color = config.blockquoteBackgroundColor
        isAntiAlias = true
    }
    
    private val borderPaint = Paint().apply {
        color = config.blockquoteBorderColor
        strokeWidth = config.blockquoteBorderWidth.toFloat()
        isAntiAlias = true
    }
    
    override fun getLeadingMargin(first: Boolean): Int {
        return config.blockquoteMargin + config.blockquotePadding
    }
    
    override fun drawLeadingMargin(
        canvas: Canvas?,
        paint: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        canvas?.let { c ->
            val margin = config.blockquoteMargin.toFloat()
            val padding = config.blockquotePadding.toFloat()
            val borderWidth = config.blockquoteBorderWidth.toFloat()
            
            // 绘制背景
            val rect = RectF(
                x.toFloat() - margin,
                top.toFloat() - padding / 2,
                x.toFloat() + (layout?.width ?: 0) + margin,
                bottom.toFloat() + padding / 2
            )
            c.drawRoundRect(rect, config.blockquoteCornerRadius, config.blockquoteCornerRadius, this.paint)
            
            // 绘制左侧边框
            c.drawRect(
                x.toFloat() - margin,
                top.toFloat() - padding / 2,
                x.toFloat() - margin + borderWidth,
                bottom.toFloat() + padding / 2,
                borderPaint
            )
        }
    }
}

/**
 * 现代化无序列表样式
 */
class ModernBulletSpan(private val config: MarkdownStyleConfigV2) : LeadingMarginSpan {
    
    private val paint = Paint().apply {
        color = config.listBulletColor
        isAntiAlias = true
    }
    
    override fun getLeadingMargin(first: Boolean): Int {
        return config.listIndent
    }
    
    override fun drawLeadingMargin(
        canvas: Canvas?,
        paint: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        canvas?.let { c ->
            val radius = 6f
            val centerX = x.toFloat() - config.listIndent / 2
            val centerY = (top + bottom) / 2f
            
            c.drawCircle(centerX, centerY, radius, this.paint)
        }
    }
}

/**
 * 现代化有序列表样式
 */
class ModernOrderedListSpan(private val config: MarkdownStyleConfigV2) : LeadingMarginSpan {
    
    private val paint = Paint().apply {
        color = config.listBulletColor
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
    }
    
    override fun getLeadingMargin(first: Boolean): Int {
        return config.listIndent
    }
    
    override fun drawLeadingMargin(
        canvas: Canvas?,
        paint: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        // 有序列表的数字将由Markwon自动处理
        // 这里可以添加额外的样式定制
    }
}

/**
 * 现代化分割线样式
 */
class ModernDividerSpan(private val config: MarkdownStyleConfigV2) : ReplacementSpan() {
    
    private val paint = Paint().apply {
        color = config.dividerColor
        strokeWidth = config.dividerHeight.toFloat()
        isAntiAlias = true
    }
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return 0 // 分割线不占用水平空间
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
        val centerY = (top + bottom) / 2f
        val margin = config.dividerMargin.toFloat()
        
        canvas.drawLine(
            x + margin,
            centerY,
            x + (canvas.width - margin * 2),
            centerY,
            this.paint
        )
    }
}