package com.chenge.markdown.plugins

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.chenge.markdown.common.MarkdownStyleConfigV2
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import org.commonmark.node.*

/**
 * 无障碍功能插件
 * 提供字体大小调节、对比度优化、屏幕阅读器支持等无障碍功能
 */
class AccessibilityPlugin(
    private val context: Context,
    private var config: MarkdownStyleConfigV2
) : AbstractMarkwonPlugin() {
    
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    private var fontSizeMultiplier = 1.0f
    private var highContrastMode = false
    private var screenReaderMode = false
    
    init {
        // 检测系统无障碍设置
        detectAccessibilitySettings()
    }
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        configureAccessibilitySpans(builder)
    }
    
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        configureAccessibilityVisitor(builder)
    }
    
    private fun configureAccessibilitySpans(builder: MarkwonSpansFactory.Builder) {
        // 标题无障碍支持
        builder.setFactory(Heading::class.java) { _, _ ->
            val level = 1 // 暂时使用默认值
            arrayOf(
                AccessibleHeadingSpan(level, config, fontSizeMultiplier, highContrastMode)
            )
        }
        
        // 段落无障碍支持
        builder.setFactory(Paragraph::class.java) { _, _ ->
            arrayOf(
                AccessibleParagraphSpan(config, fontSizeMultiplier, highContrastMode)
            )
        }
        
        // 代码无障碍支持
        builder.setFactory(Code::class.java) { _, _ ->
            arrayOf(
                AccessibleCodeSpan(config, fontSizeMultiplier, highContrastMode)
            )
        }
        
        // 链接无障碍支持
        builder.setFactory(Link::class.java) { _, _ ->
            val url = "" // 暂时使用默认值
            arrayOf(
                AccessibleLinkSpan(url, config, fontSizeMultiplier, highContrastMode)
            )
        }
        
        // 强调文本无障碍支持
        builder.setFactory(Emphasis::class.java) { _, _ ->
            arrayOf(
                AccessibleEmphasisSpan(config, fontSizeMultiplier, highContrastMode)
            )
        }
        
        // 加粗文本无障碍支持
        builder.setFactory(StrongEmphasis::class.java) { _, _ ->
            arrayOf(
                AccessibleStrongSpan(config, fontSizeMultiplier, highContrastMode)
            )
        }
    }
    
    private fun configureAccessibilityVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(Heading::class.java) { visitor, heading ->
            val length = visitor.length()
            // visitor.setSpanFlag("level", heading.level) // 暂时注释掉
            visitor.visitChildren(heading)
            visitor.setSpansForNodeOptional(heading, length)
        }
    }
    
    /**
     * 检测系统无障碍设置
     */
    private fun detectAccessibilitySettings() {
        screenReaderMode = accessibilityManager.isEnabled && 
                accessibilityManager.isTouchExplorationEnabled
        
        // 检测高对比度模式（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 这里可以添加高对比度检测逻辑
        }
        
        // 根据系统字体大小调整
        val systemFontScale = context.resources.configuration.fontScale
        fontSizeMultiplier = systemFontScale
    }
    
    /**
     * 设置字体大小倍数
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        fontSizeMultiplier = multiplier
        updateConfig()
    }
    
    /**
     * 启用/禁用高对比度模式
     */
    fun setHighContrastMode(enabled: Boolean) {
        highContrastMode = enabled
        updateConfig()
    }
    
    /**
     * 更新配置
     */
    private fun updateConfig() {
        // 根据无障碍设置更新配置
        if (highContrastMode) {
            config = config.copy(
                primaryColor = if (config.isDarkTheme) Color.WHITE else Color.BLACK,
                onSurfaceColor = if (config.isDarkTheme) Color.WHITE else Color.BLACK,
                surfaceColor = if (config.isDarkTheme) Color.BLACK else Color.WHITE
            )
        }
    }
    
    /**
     * 为TextView添加无障碍支持
     */
    fun setupAccessibilityForTextView(textView: TextView) {
        // 设置内容描述
        textView.contentDescription = "Markdown内容"
        
        // 启用焦点
        textView.isFocusable = true
        textView.isFocusableInTouchMode = true
        
        // 设置无障碍代理
        ViewCompat.setAccessibilityDelegate(textView, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                
                // 设置角色
                info.className = "Markdown Content"
                
                // 添加自定义动作
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfo.ACTION_CLICK,
                        "阅读内容"
                    )
                )
                
                // 设置内容描述
                val content = (host as TextView).text.toString()
                info.contentDescription = generateContentDescription(content)
            }
            
            override fun performAccessibilityAction(
                host: View,
                action: Int,
                args: android.os.Bundle?
            ): Boolean {
                when (action) {
                    AccessibilityNodeInfo.ACTION_CLICK -> {
                        // 处理点击事件
                        announceContent(host as TextView)
                        return true
                    }
                }
                return super.performAccessibilityAction(host, action, args)
            }
        })
        
        // 设置重要性
        ViewCompat.setImportantForAccessibility(
            textView,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
        )
    }
    
    /**
     * 生成内容描述
     */
    private fun generateContentDescription(content: String): String {
        val lines = content.split("\n")
        val description = StringBuilder()
        
        for (line in lines.take(3)) { // 只取前3行
            when {
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val title = line.dropWhile { it == '#' }.trim()
                    description.append("${level}级标题: $title. ")
                }
                line.trim().isNotEmpty() -> {
                    description.append("段落: ${line.trim()}. ")
                }
            }
        }
        
        if (lines.size > 3) {
            description.append("还有更多内容...")
        }
        
        return description.toString()
    }
    
    /**
     * 朗读内容
     */
    private fun announceContent(textView: TextView) {
        if (screenReaderMode) {
            val content = textView.text.toString()
            val announcement = generateContentDescription(content)
            
            textView.announceForAccessibility(announcement)
        }
    }
}

/**
 * 无障碍标题样式
 */
class AccessibleHeadingSpan(
    private val level: Int,
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.MetricAffectingSpan() {
    
    override fun updateMeasureState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    override fun updateDrawState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    private fun updateTextPaint(paint: android.text.TextPaint) {
        val baseSize = when (level) {
            1 -> config.textSize * 2.0f
            2 -> config.textSize * 1.8f
            3 -> config.textSize * 1.6f
            4 -> config.textSize * 1.4f
            5 -> config.textSize * 1.2f
            else -> config.textSize * 1.1f
        }
        
        paint.textSize = baseSize * fontSizeMultiplier
        paint.typeface = Typeface.DEFAULT_BOLD
        
        if (highContrastMode) {
            paint.color = if (config.isDarkTheme) Color.WHITE else Color.BLACK
        } else {
            paint.color = config.headingColor
        }
        
        // 增加字体粗细以提高可读性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            paint.typeface = Typeface.create(paint.typeface, 700, false)
        }
    }
}

/**
 * 无障碍段落样式
 */
class AccessibleParagraphSpan(
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.MetricAffectingSpan() {
    
    override fun updateMeasureState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    override fun updateDrawState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    private fun updateTextPaint(paint: android.text.TextPaint) {
        paint.textSize = config.textSize * fontSizeMultiplier
        
        if (highContrastMode) {
            paint.color = if (config.isDarkTheme) Color.WHITE else Color.BLACK
        } else {
            paint.color = config.textColor
        }
        
        // 增加行间距以提高可读性
        paint.baselineShift = (paint.textSize * 0.1f).toInt()
    }
}

/**
 * 无障碍代码样式
 */
class AccessibleCodeSpan(
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.MetricAffectingSpan() {
    
    override fun updateMeasureState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    override fun updateDrawState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
        
        // 设置背景色
        if (highContrastMode) {
            paint.bgColor = if (config.isDarkTheme) Color.DKGRAY else Color.LTGRAY
        } else {
            paint.bgColor = config.codeBackgroundColor
        }
    }
    
    private fun updateTextPaint(paint: android.text.TextPaint) {
        paint.textSize = config.textSize * fontSizeMultiplier
        paint.typeface = Typeface.MONOSPACE
        
        if (highContrastMode) {
            paint.color = if (config.isDarkTheme) Color.WHITE else Color.BLACK
        } else {
            paint.color = config.codeTextColor
        }
    }
}

/**
 * 无障碍链接样式
 */
class AccessibleLinkSpan(
    private val url: String,
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.ClickableSpan() {
    
    override fun onClick(widget: View) {
        // 在点击时提供音频反馈
        widget.announceForAccessibility("打开链接: $url")
        
        // 这里可以添加实际的链接打开逻辑
    }
    
    override fun updateDrawState(ds: android.text.TextPaint) {
        super.updateDrawState(ds)
        
        ds.textSize = config.textSize * fontSizeMultiplier
        
        if (highContrastMode) {
            ds.color = if (config.isDarkTheme) Color.CYAN else Color.BLUE
        } else {
            ds.color = config.linkColor
        }
        
        ds.isUnderlineText = true
        
        // 增加字体粗细以提高可见性
        ds.typeface = Typeface.DEFAULT_BOLD
    }
}

/**
 * 无障碍强调样式
 */
class AccessibleEmphasisSpan(
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.MetricAffectingSpan() {
    
    override fun updateMeasureState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    override fun updateDrawState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    private fun updateTextPaint(paint: android.text.TextPaint) {
        paint.textSize = config.textSize * fontSizeMultiplier
        paint.typeface = Typeface.DEFAULT_BOLD
        
        if (highContrastMode) {
            paint.color = if (config.isDarkTheme) Color.WHITE else Color.BLACK
        } else {
            paint.color = config.textColor
        }
    }
}

/**
 * 无障碍加粗样式
 */
class AccessibleStrongSpan(
    private val config: MarkdownStyleConfigV2,
    private val fontSizeMultiplier: Float,
    private val highContrastMode: Boolean
) : android.text.style.MetricAffectingSpan() {
    
    override fun updateMeasureState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    override fun updateDrawState(paint: android.text.TextPaint) {
        updateTextPaint(paint)
    }
    
    private fun updateTextPaint(paint: android.text.TextPaint) {
        paint.textSize = config.textSize * fontSizeMultiplier
        
        if (highContrastMode) {
            paint.color = if (config.isDarkTheme) Color.WHITE else Color.BLACK
        } else {
            paint.color = config.textColor
        }
        
        // 设置更粗的字体
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            paint.typeface = Typeface.create(paint.typeface, 800, false)
        } else {
            paint.typeface = Typeface.DEFAULT_BOLD
        }
    }
}

/**
 * 无障碍工具类
 */
object AccessibilityUtils {
    
    /**
     * 检查是否启用了屏幕阅读器
     */
    fun isScreenReaderEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
    }
    
    /**
     * 获取系统字体缩放比例
     */
    fun getSystemFontScale(context: Context): Float {
        return context.resources.configuration.fontScale
    }
    
    /**
     * 计算对比度
     */
    fun calculateContrast(foreground: Int, background: Int): Double {
        val foregroundLuminance = calculateLuminance(foreground)
        val backgroundLuminance = calculateLuminance(background)
        
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    private fun calculateLuminance(color: Int): Double {
        val red = Color.red(color) / 255.0
        val green = Color.green(color) / 255.0
        val blue = Color.blue(color) / 255.0
        
        val r = if (red <= 0.03928) red / 12.92 else Math.pow((red + 0.055) / 1.055, 2.4)
        val g = if (green <= 0.03928) green / 12.92 else Math.pow((green + 0.055) / 1.055, 2.4)
        val b = if (blue <= 0.03928) blue / 12.92 else Math.pow((blue + 0.055) / 1.055, 2.4)
        
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
    
    /**
     * 检查颜色对比度是否符合WCAG标准
     */
    fun meetsWCAGContrast(foreground: Int, background: Int, level: WCAGLevel = WCAGLevel.AA): Boolean {
        val contrast = calculateContrast(foreground, background)
        return when (level) {
            WCAGLevel.AA -> contrast >= 4.5
            WCAGLevel.AAA -> contrast >= 7.0
        }
    }
    
    /**
     * 调整颜色以满足对比度要求
     */
    fun adjustColorForContrast(
        foreground: Int,
        background: Int,
        targetLevel: WCAGLevel = WCAGLevel.AA
    ): Int {
        if (meetsWCAGContrast(foreground, background, targetLevel)) {
            return foreground
        }
        
        // 简单的调整策略：使前景色更暗或更亮
        val backgroundLuminance = calculateLuminance(background)
        
        return if (backgroundLuminance > 0.5) {
            // 背景较亮，使前景色更暗
            Color.BLACK
        } else {
            // 背景较暗，使前景色更亮
            Color.WHITE
        }
    }
    
    /**
     * 为文本添加语义标记
     */
    fun addSemanticMarkup(text: String, textView: TextView) {
        val spannable = SpannableString(text)
        
        // 添加标题标记
        val headingPattern = Regex("^(#{1,6})\\s+(.+)$", RegexOption.MULTILINE)
        headingPattern.findAll(text).forEach { match ->
            val level = match.groupValues[1].length
            val start = match.range.first
            val end = match.range.last + 1
            
            // 添加语义信息
            spannable.setSpan(
                object : android.text.style.ReplacementSpan() {
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
                        canvas: android.graphics.Canvas,
                        text: CharSequence?,
                        start: Int,
                        end: Int,
                        x: Float,
                        top: Int,
                        y: Int,
                        bottom: Int,
                        paint: Paint
                    ) {
                        // 绘制标题，并添加无障碍信息
                        canvas.drawText(text!!, start, end, x, y.toFloat(), paint)
                    }
                },
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        textView.text = spannable
    }
    
    /**
     * 创建无障碍友好的TextView
     */
    fun createAccessibleTextView(
        context: Context,
        config: MarkdownStyleConfigV2
    ): TextView {
        return TextView(context).apply {
            // 设置基本属性
            textSize = config.textSize * getSystemFontScale(context)
            
            // 设置高对比度颜色（如果需要）
            if (isScreenReaderEnabled(context)) {
                setTextColor(if (config.isDarkTheme) Color.WHITE else Color.BLACK)
                setBackgroundColor(if (config.isDarkTheme) Color.BLACK else Color.WHITE)
            } else {
                setTextColor(config.textColor)
                setBackgroundColor(config.surfaceColor)
            }
            
            // 设置行间距
            setLineSpacing(config.textSize * 0.5f, 1.2f)
            
            // 启用文本选择
            setTextIsSelectable(true)
            
            // 设置内容描述
            contentDescription = "Markdown文档内容"
            
            // 设置焦点
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }
}

/**
 * WCAG对比度等级
 */
enum class WCAGLevel {
    AA,  // 4.5:1 对比度
    AAA  // 7:1 对比度
}

/**
 * 无障碍配置
 */
data class AccessibilityConfig(
    val fontSizeMultiplier: Float = 1.0f,
    val highContrastMode: Boolean = false,
    val screenReaderOptimized: Boolean = false,
    val minimumTouchTargetSize: Int = 48, // dp
    val wcagLevel: WCAGLevel = WCAGLevel.AA
) {
    companion object {
        /**
         * 根据系统设置创建配置
         */
        fun fromSystemSettings(context: Context): AccessibilityConfig {
            return AccessibilityConfig(
                fontSizeMultiplier = AccessibilityUtils.getSystemFontScale(context),
                screenReaderOptimized = AccessibilityUtils.isScreenReaderEnabled(context)
            )
        }
    }
}