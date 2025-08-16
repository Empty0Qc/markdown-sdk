package com.chenge.markdown.common

import android.widget.TextView

/**
 * 渲染器接口 - 定义稳定的渲染 API
 * 用于解耦核心逻辑与具体渲染实现
 */
interface MarkdownRenderer {
    /**
     * 渲染 Markdown 内容到 TextView
     * @param textView 目标 TextView
     * @param markdown 已解析的 Markdown 内容
     * @param config 渲染配置
     */
    fun render(textView: TextView, markdown: String, config: MarkdownConfig = MarkdownConfig())
    
    /**
     * 异步渲染 Markdown 内容
     * @param textView 目标 TextView
     * @param markdown 已解析的 Markdown 内容
     * @param config 渲染配置
     * @param onComplete 渲染完成回调
     */
    fun renderAsync(
        textView: TextView, 
        markdown: String, 
        config: MarkdownConfig = MarkdownConfig(),
        onComplete: (() -> Unit)? = null
    )
}

/**
 * 渲染结果数据类
 */
data class RenderResult(
    val success: Boolean,
    val renderTimeMs: Long = 0,
    val error: Throwable? = null
)

/**
 * 调试渲染器接口 - 提供性能监控能力
 */
interface DebugRenderer : MarkdownRenderer {
    /**
     * 带性能监控的渲染
     * @param textView 目标 TextView
     * @param markdown 已解析的 Markdown 内容
     * @param config 渲染配置
     * @return 渲染结果包含耗时信息
     */
    fun renderWithTiming(
        textView: TextView, 
        markdown: String, 
        config: MarkdownConfig = MarkdownConfig()
    ): RenderResult
}