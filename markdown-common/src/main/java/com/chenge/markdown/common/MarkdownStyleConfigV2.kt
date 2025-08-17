package com.chenge.markdown.common

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange

/**
 * 增强版Markdown样式配置，支持Material Design 3和现代化UI
 * 提供更丰富的样式自定义选项
 */
data class MarkdownStyleConfigV2(
    // === 颜色配置 ===
    @ColorInt val primaryColor: Int = Color.parseColor("#1976D2"),
    @ColorInt val onPrimaryColor: Int = Color.WHITE,
    @ColorInt val surfaceColor: Int = Color.WHITE,
    @ColorInt val onSurfaceColor: Int = Color.parseColor("#212121"),
    @ColorInt val surfaceVariantColor: Int = Color.parseColor("#F5F5F5"),
    @ColorInt val onSurfaceVariantColor: Int = Color.parseColor("#424242"),
    @ColorInt val outlineColor: Int = Color.parseColor("#9E9E9E"),
    @ColorInt val outlineVariantColor: Int = Color.parseColor("#E0E0E0"),
    
    // === 标题样式 ===
    @ColorInt val headingColor: Int = onSurfaceColor,
    val headingTypeface: Typeface? = null,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH1: Float = 1.8f,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH2: Float = 1.6f,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH3: Float = 1.4f,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH4: Float = 1.2f,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH5: Float = 1.1f,
    @FloatRange(from = 0.8, to = 2.0) val headingScaleH6: Float = 1.0f,
    @Dimension val headingMarginTop: Int = 24,
    @Dimension val headingMarginBottom: Int = 16,
    
    // === 正文样式 ===
    @ColorInt val textColor: Int = onSurfaceColor,
    val textTypeface: Typeface? = null,
    @Dimension val textSize: Float = 16f,
    @FloatRange(from = 1.0, to = 2.0) val lineHeight: Float = 1.5f,
    @Dimension val paragraphSpacing: Int = 16,
    
    // === 代码样式 ===
    @ColorInt val codeTextColor: Int = Color.parseColor("#D32F2F"),
    @ColorInt val codeBackgroundColor: Int = surfaceVariantColor,
    val codeTypeface: Typeface? = Typeface.MONOSPACE,
    @Dimension val codeCornerRadius: Float = 8f,
    @Dimension val codePadding: Int = 12,
    @Dimension val codeMargin: Int = 16,
    
    // === 代码块样式 ===
    @ColorInt val codeBlockTextColor: Int = onSurfaceColor,
    @ColorInt val codeBlockBackgroundColor: Int = Color.parseColor("#1E1E1E"),
    @ColorInt val codeBlockBorderColor: Int = outlineVariantColor,
    @Dimension val codeBlockCornerRadius: Float = 12f,
    @Dimension val codeBlockPadding: Int = 16,
    @Dimension val codeBlockMargin: Int = 16,
    val enableSyntaxHighlighting: Boolean = true,
    
    // === 链接样式 ===
    @ColorInt val linkColor: Int = primaryColor,
    @ColorInt val linkVisitedColor: Int = Color.parseColor("#7B1FA2"),
    val linkUnderline: Boolean = true,
    
    // === 引用块样式 ===
    @ColorInt val blockquoteTextColor: Int = onSurfaceVariantColor,
    @ColorInt val blockquoteBackgroundColor: Int = surfaceVariantColor,
    @ColorInt val blockquoteBorderColor: Int = primaryColor,
    @Dimension val blockquoteBorderWidth: Int = 4,
    @Dimension val blockquoteCornerRadius: Float = 8f,
    @Dimension val blockquotePadding: Int = 16,
    @Dimension val blockquoteMargin: Int = 16,
    
    // === 列表样式 ===
    @ColorInt val listBulletColor: Int = primaryColor,
    @Dimension val listIndent: Int = 24,
    @Dimension val listItemSpacing: Int = 8,
    
    // === 表格样式 ===
    @ColorInt val tableHeaderBackgroundColor: Int = surfaceVariantColor,
    @ColorInt val tableHeaderTextColor: Int = onSurfaceColor,
    @ColorInt val tableBorderColor: Int = outlineVariantColor,
    @ColorInt val tableAlternateRowColor: Int = Color.parseColor("#FAFAFA"),
    @Dimension val tableBorderWidth: Int = 1,
    @Dimension val tableCornerRadius: Float = 8f,
    @Dimension val tableCellPadding: Int = 12,
    
    // === 分割线样式 ===
    @ColorInt val dividerColor: Int = outlineVariantColor,
    @Dimension val dividerHeight: Int = 1,
    @Dimension val dividerMargin: Int = 24,
    
    // === 图片样式 ===
    @Dimension val imageCornerRadius: Float = 12f,
    @Dimension val imageMargin: Int = 16,
    val enableImageZoom: Boolean = true,
    val enableImageLazyLoad: Boolean = true,
    
    // === 动画配置 ===
    val enableAnimations: Boolean = true,
    val animationDuration: Long = 300L,
    
    // === 深色模式支持 ===
    val isDarkTheme: Boolean = false
) {
    companion object {
        /**
         * 创建深色主题配置
         */
        fun createDarkTheme(): MarkdownStyleConfigV2 {
            return MarkdownStyleConfigV2(
                primaryColor = Color.parseColor("#64B5F6"),
                onPrimaryColor = Color.parseColor("#0D47A1"),
                surfaceColor = Color.parseColor("#1E1E1E"),
                onSurfaceColor = Color.parseColor("#E0E0E0"),
                surfaceVariantColor = Color.parseColor("#2C2C2C"),
                onSurfaceVariantColor = Color.parseColor("#BDBDBD"),
                outlineColor = Color.parseColor("#757575"),
                outlineVariantColor = Color.parseColor("#424242"),
                
                headingColor = Color.parseColor("#E0E0E0"),
                textColor = Color.parseColor("#E0E0E0"),
                
                codeTextColor = Color.parseColor("#EF5350"),
                codeBackgroundColor = Color.parseColor("#2C2C2C"),
                
                codeBlockTextColor = Color.parseColor("#E0E0E0"),
                codeBlockBackgroundColor = Color.parseColor("#0D1117"),
                codeBlockBorderColor = Color.parseColor("#424242"),
                
                linkColor = Color.parseColor("#64B5F6"),
                linkVisitedColor = Color.parseColor("#CE93D8"),
                
                blockquoteTextColor = Color.parseColor("#BDBDBD"),
                blockquoteBackgroundColor = Color.parseColor("#2C2C2C"),
                blockquoteBorderColor = Color.parseColor("#64B5F6"),
                
                listBulletColor = Color.parseColor("#64B5F6"),
                
                tableHeaderBackgroundColor = Color.parseColor("#2C2C2C"),
                tableHeaderTextColor = Color.parseColor("#E0E0E0"),
                tableBorderColor = Color.parseColor("#424242"),
                tableAlternateRowColor = Color.parseColor("#252525"),
                
                dividerColor = Color.parseColor("#424242"),
                
                isDarkTheme = true
            )
        }
        
        /**
         * 创建浅色主题配置
         */
        fun createLightTheme(): MarkdownStyleConfigV2 {
            return MarkdownStyleConfigV2()
        }
    }
}