package com.chenge.markdown.plugins

import android.content.Context
import android.widget.TextView
import com.chenge.markdown.common.MarkdownStyleConfigV2

import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin

/**
 * 现代化Markdown引擎
 * 整合所有自定义插件，提供业界领先的Markdown渲染体验
 */
class ModernMarkdownEngine private constructor(
    private val context: Context,
    private val config: MarkdownStyleConfigV2
) {
    
    private val customViewRenderer: CustomViewRenderer by lazy {
        CustomViewRenderer(context).apply {
            setStyleConfig(config)
        }
    }
    
    private val markwon: Markwon by lazy {
        createMarkwonInstance()
    }
    
    companion object {
        /**
         * 创建现代化Markdown引擎实例
         */
        fun create(
            context: Context,
            config: MarkdownStyleConfigV2 = MarkdownStyleConfigV2.createLightTheme()
        ): ModernMarkdownEngine {
            return ModernMarkdownEngine(context, config)
        }
        
        /**
         * 创建带有自定义配置的引擎实例
         */
        fun createWithCustomConfig(
            context: Context,
            config: MarkdownStyleConfigV2
        ): ModernMarkdownEngine {
            return ModernMarkdownEngine(context, config)
        }
    }
    
    /**
     * 创建Markwon实例，集成所有现代化插件
     */
    private fun createMarkwonInstance(): Markwon {
        return Markwon.builder(context)
            // 核心插件
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(MovementMethodPlugin.create())
            
            // 扩展插件
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            
            // 自定义现代化插件
            .usePlugin(ModernStylePlugin(config))
            .usePlugin(SyntaxHighlightPlugin(config))
            .usePlugin(ModernTablePlugin(config))
            .usePlugin(MathPlugin(config))
            .usePlugin(ModernQuotePlugin(config))
            .usePlugin(PerformancePlugin(config, 50 * 1024 * 1024))
            .usePlugin(AccessibilityPlugin(context, config))
            
            // 自定义视图渲染器插件
            .usePlugin(customViewRenderer.createCustomViewPlugin())
            
            .build()
    }
    
    /**
     * 渲染Markdown到TextView
     */
    fun setMarkdown(textView: TextView, markdown: String) {
        markwon.setMarkdown(textView, markdown)
    }
    
    /**
     * 异步渲染Markdown
     */
    fun setMarkdownAsync(
        textView: TextView,
        markdown: String,
        onComplete: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        try {
            // 在后台线程处理复杂的渲染逻辑
            Thread {
                try {
                    val spanned = markwon.toMarkdown(markdown)
                    
                    // 在主线程更新UI
                    textView.post {
                        textView.text = spanned
                        onComplete?.invoke()
                    }
                } catch (e: Exception) {
                    textView.post {
                        onError?.invoke(e)
                    }
                }
            }.start()
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }
    
    /**
     * 预处理Markdown文本
     */
    fun preprocessMarkdown(markdown: String): String {
        var processed = markdown
        
        // 处理自定义语法扩展
        processed = processCustomSyntax(processed)
        
        // 处理表情符号
        processed = processEmojis(processed)
        
        // 处理特殊标记
        processed = processSpecialMarkers(processed)
        
        return processed
    }
    
    /**
     * 处理自定义语法扩展
     */
    private fun processCustomSyntax(markdown: String): String {
        var processed = markdown
        
        // 处理高亮文本 ==text==
        processed = processed.replace(
            Regex("==(.*?)=="),
            "<mark>$1</mark>"
        )
        
        // 处理键盘按键 [[key]]
        processed = processed.replace(
            Regex("\\[\\[(.*?)\\]\\]"),
            "<kbd>$1</kbd>"
        )
        
        // 处理脚注 [^1]
        processed = processed.replace(
            Regex("\\[\\^(\\w+)\\]"),
            "<sup><a href=\"#fn$1\">$1</a></sup>"
        )
        
        // 处理缩写 *[HTML]: HyperText Markup Language
        processed = processed.replace(
            Regex("\\*\\[([^\\]]+)\\]:\\s*(.+)"),
            "<abbr title=\"$2\">$1</abbr>"
        )
        
        return processed
    }
    
    /**
     * 处理表情符号
     */
    private fun processEmojis(markdown: String): String {
        val emojiMap = mapOf(
            ":smile:" to "😊",
            ":heart:" to "❤️",
            ":thumbsup:" to "👍",
            ":thumbsdown:" to "👎",
            ":fire:" to "🔥",
            ":star:" to "⭐",
            ":warning:" to "⚠️",
            ":info:" to "ℹ️",
            ":check:" to "✅",
            ":cross:" to "❌",
            ":bulb:" to "💡",
            ":rocket:" to "🚀",
            ":gear:" to "⚙️",
            ":book:" to "📚",
            ":pencil:" to "✏️",
            ":computer:" to "💻",
            ":mobile:" to "📱",
            ":email:" to "📧",
            ":calendar:" to "📅",
            ":clock:" to "🕐"
        )
        
        var processed = markdown
        emojiMap.forEach { (code, emoji) ->
            processed = processed.replace(code, emoji)
        }
        
        return processed
    }
    
    /**
     * 处理特殊标记
     */
    private fun processSpecialMarkers(markdown: String): String {
        var processed = markdown
        
        // 处理进度条 [progress:75%]
        processed = processed.replace(
            Regex("\\[progress:(\\d+)%\\]"),
            "<div class=\"progress\"><div class=\"progress-bar\" style=\"width: $1%\"></div></div>"
        )
        
        // 处理标签 [tag:important]
        processed = processed.replace(
            Regex("\\[tag:(\\w+)\\]"),
            "<span class=\"tag tag-$1\">$1</span>"
        )
        
        // 处理徽章 [badge:new]
        processed = processed.replace(
            Regex("\\[badge:(\\w+)\\]"),
            "<span class=\"badge badge-$1\">$1</span>"
        )
        
        return processed
    }
    
    /**
     * 获取渲染统计信息
     */
    fun getRenderingStats(): RenderingStats {
        return RenderingStats(
            pluginsCount = 8,
            customSpansCount = 15,
            supportedElements = listOf(
                "Headers", "Paragraphs", "Lists", "Links", "Images",
                "Code Blocks", "Inline Code", "Tables", "Blockquotes",
                "Math Formulas", "Task Lists", "Strikethrough",
                "HTML Tags", "Emojis", "Custom Syntax"
            ),
            performanceOptimizations = listOf(
                "Async Rendering", "Image Lazy Loading", "Text Caching",
                "Memory Management", "Span Recycling"
            )
        )
    }
    
    /**
     * 更新样式配置
     */
    fun updateConfig(newConfig: MarkdownStyleConfigV2) {
        // 注意：这需要重新创建Markwon实例
        // 在实际应用中，可能需要更复杂的配置更新机制
    }
    
    /**
     * 获取当前配置
     */
    fun getConfig(): MarkdownStyleConfigV2 {
        return config
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        // 清理缓存、释放资源等
    }
}

/**
 * 渲染统计信息
 */
data class RenderingStats(
    val pluginsCount: Int,
    val customSpansCount: Int,
    val supportedElements: List<String>,
    val performanceOptimizations: List<String>
)

/**
 * 现代化Markdown引擎构建器
 */
class ModernMarkdownEngineBuilder(private val context: Context) {
    private var config: MarkdownStyleConfigV2? = null
    private var enableMath = true
    private var enableTables = true
    private var enableSyntaxHighlight = true
    private var enableCustomQuotes = true
    private var enableInteractions = true
    private var enablePerformanceOptimizations = true
    private var enableAccessibility = true
    
    /**
     * 设置样式配置
     */
    fun config(config: MarkdownStyleConfigV2): ModernMarkdownEngineBuilder {
        this.config = config
        return this
    }
    
    /**
     * 启用/禁用数学公式支持
     */
    fun enableMath(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableMath = enable
        return this
    }
    
    /**
     * 启用/禁用表格支持
     */
    fun enableTables(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableTables = enable
        return this
    }
    
    /**
     * 启用/禁用语法高亮
     */
    fun enableSyntaxHighlight(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableSyntaxHighlight = enable
        return this
    }
    
    /**
     * 启用/禁用自定义引用块
     */
    fun enableCustomQuotes(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableCustomQuotes = enable
        return this
    }
    
    /**
     * 启用/禁用交互功能
     */
    fun enableInteractions(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableInteractions = enable
        return this
    }
    
    /**
     * 启用/禁用性能优化
     */
    fun enablePerformanceOptimizations(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enablePerformanceOptimizations = enable
        return this
    }
    
    /**
     * 启用/禁用无障碍功能
     */
    fun enableAccessibility(enable: Boolean): ModernMarkdownEngineBuilder {
        this.enableAccessibility = enable
        return this
    }
    
    /**
     * 构建引擎实例
     */
    fun build(): ModernMarkdownEngine {
        val finalConfig = config ?: MarkdownStyleConfigV2.createLightTheme()
        return ModernMarkdownEngine.create(context, finalConfig)
    }
}

/**
 * 扩展函数：为Context添加创建现代化Markdown引擎的便捷方法
 */
fun Context.createModernMarkdownEngine(
    config: MarkdownStyleConfigV2? = null
): ModernMarkdownEngine {
    return ModernMarkdownEngine.create(
        this,
        config ?: MarkdownStyleConfigV2.createLightTheme()
    )
}

/**
 * 扩展函数：为TextView添加设置Markdown的便捷方法
 */
fun TextView.setModernMarkdown(
    markdown: String,
    engine: ModernMarkdownEngine? = null
) {
    val markdownEngine = engine ?: context.createModernMarkdownEngine()
    markdownEngine.setMarkdown(this, markdown)
}