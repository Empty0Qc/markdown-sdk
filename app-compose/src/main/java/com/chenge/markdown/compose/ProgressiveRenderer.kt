package com.chenge.markdown.compose

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 统一的渐进式内容渲染器
 * 合并了流式输出和打字机效果，提供真正的渐进式内容显示
 */
class ProgressiveRenderer {

    /**
     * 渲染模式
     */
    enum class RenderMode {
        INSTANT, // 立即显示
        PROGRESSIVE, // 渐进式显示（逐字符）
        CHUNKED // 分块显示（逐段落）
    }

    /**
     * 渲染速度
     */
    enum class RenderSpeed(val delayMs: Long) {
        SLOW(80L),
        NORMAL(40L),
        FAST(20L),
        INSTANT(0L)
    }

    companion object {
        private const val CURSOR_BLINK_INTERVAL = 500L
        private const val CHUNK_SIZE = 50 // 分块模式下每块的字符数
    }

    private var renderJob: Job? = null
    private var cursorJob: Job? = null
    private var isRendering = false
    private var renderSpeed = RenderSpeed.NORMAL
    private var renderMode = RenderMode.PROGRESSIVE
    private var showCursor = true
    private var cursorChar = "▋"

    // 自动滚动管理器
    private var autoScrollManager: AutoScrollManager? = null

    // 进度回调
    private var onProgressUpdate: ((progress: Int, message: String) -> Unit)? = null

    /**
     * 设置渲染模式
     */
    fun setRenderMode(mode: RenderMode) {
        renderMode = mode
    }

    /**
     * 设置渲染速度
     */
    fun setRenderSpeed(speed: RenderSpeed) {
        renderSpeed = speed
    }

    /**
     * 设置是否显示光标
     */
    fun setShowCursor(show: Boolean) {
        showCursor = show
    }

    /**
     * 设置进度更新回调
     */
    fun setProgressCallback(callback: (progress: Int, message: String) -> Unit) {
        onProgressUpdate = callback
    }

    /**
     * 设置自动滚动管理器
     */
    fun setAutoScrollManager(scrollView: NestedScrollView, textView: TextView, lifecycleOwner: LifecycleOwner) {
        autoScrollManager = AutoScrollManager().apply {
            initialize(scrollView, textView, lifecycleOwner)
        }
    }

    /**
     * 启用/禁用自动滚动
     */
    fun setAutoScrollEnabled(enabled: Boolean) {
        autoScrollManager?.setAutoScrollEnabled(enabled)
    }

    /**
     * 开始渐进式渲染
     * @param textView 目标TextView
     * @param content 要显示的内容
     * @param lifecycleOwner 生命周期所有者
     * @param onComplete 完成回调
     */
    fun startRender(
        textView: TextView,
        content: Spanned,
        lifecycleOwner: LifecycleOwner,
        onComplete: (() -> Unit)? = null
    ) {
        // 停止之前的渲染
        stopRender()

        // 更新AutoScrollManager的TextView引用
        autoScrollManager?.let { manager ->
            // 重新初始化以更新TextView引用
            val scrollView = findScrollView(textView)
            if (scrollView != null) {
                manager.initialize(scrollView, textView, lifecycleOwner)
            }
        }

        when (renderMode) {
            RenderMode.INSTANT -> {
                textView.text = content
                onProgressUpdate?.invoke(100, "渲染完成")
                onComplete?.invoke()
            }
            RenderMode.PROGRESSIVE -> {
                startProgressiveRender(textView, content, lifecycleOwner, onComplete)
            }
            RenderMode.CHUNKED -> {
                startChunkedRender(textView, content, lifecycleOwner, onComplete)
            }
        }
    }

    /**
     * 逐字符渲染（原打字机效果）
     */
    private fun startProgressiveRender(
        textView: TextView,
        content: Spanned,
        lifecycleOwner: LifecycleOwner,
        onComplete: (() -> Unit)?
    ) {
        isRendering = true
        val contentLength = content.length

        // 启动自动滚动
        autoScrollManager?.startAutoScroll()

        renderJob = lifecycleOwner.lifecycleScope.launch {
            try {
                val builder = SpannableStringBuilder()

                for (i in 0 until contentLength) {
                    if (!isRendering) break

                    // 逐字符添加内容，保持样式
                    val char = content[i]
                    builder.append(char)

                    // 复制样式
                    copySpansToBuilder(content, builder, i)

                    // 添加光标
                    val displayText = if (showCursor && i < contentLength - 1) {
                        SpannableStringBuilder(builder).append(cursorChar)
                    } else {
                        builder
                    }

                    textView.text = displayText

                    // 更新进度
                    val progress = ((i + 1) * 100 / contentLength)
                    onProgressUpdate?.invoke(progress, "正在渲染... ($progress%)")

                    // 控制速度
                    if (renderSpeed.delayMs > 0) {
                        delay(renderSpeed.delayMs)
                    }
                }

                // 渲染完成
                if (isRendering) {
                    textView.text = content
                    isRendering = false
                    autoScrollManager?.stopAutoScroll()
                    onProgressUpdate?.invoke(100, "渲染完成")
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                isRendering = false
                autoScrollManager?.stopAutoScroll()
                textView.text = content
                onProgressUpdate?.invoke(100, "渲染完成")
                onComplete?.invoke()
            }
        }

        // 启动光标闪烁
        if (showCursor) {
            startCursorBlink(lifecycleOwner)
        }
    }

    /**
     * 分块渲染（更快的渐进式显示）
     */
    private fun startChunkedRender(
        textView: TextView,
        content: Spanned,
        lifecycleOwner: LifecycleOwner,
        onComplete: (() -> Unit)?
    ) {
        isRendering = true
        val contentLength = content.length

        // 启动自动滚动
        autoScrollManager?.startAutoScroll()

        renderJob = lifecycleOwner.lifecycleScope.launch {
            try {
                val builder = SpannableStringBuilder()
                var currentPos = 0

                while (currentPos < contentLength && isRendering) {
                    val endPos = minOf(currentPos + CHUNK_SIZE, contentLength)

                    // 添加一块内容
                    for (i in currentPos until endPos) {
                        builder.append(content[i])
                        copySpansToBuilder(content, builder, i)
                    }

                    textView.text = builder

                    // 更新进度
                    val progress = (endPos * 100 / contentLength)
                    onProgressUpdate?.invoke(progress, "正在渲染... ($progress%)")

                    currentPos = endPos

                    // 控制速度
                    if (renderSpeed.delayMs > 0) {
                        delay(renderSpeed.delayMs * 5) // 分块模式稍慢一些
                    }
                }

                // 渲染完成
                if (isRendering) {
                    textView.text = content
                    isRendering = false
                    autoScrollManager?.stopAutoScroll()
                    onProgressUpdate?.invoke(100, "渲染完成")
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                isRendering = false
                autoScrollManager?.stopAutoScroll()
                textView.text = content
                onProgressUpdate?.invoke(100, "渲染完成")
                onComplete?.invoke()
            }
        }
    }

    /**
     * 复制样式到SpannableStringBuilder
     */
    private fun copySpansToBuilder(source: Spanned, builder: SpannableStringBuilder, charIndex: Int) {
        val spans = source.getSpans(charIndex, charIndex + 1, Any::class.java)
        for (span in spans) {
            val start = source.getSpanStart(span)
            val end = source.getSpanEnd(span)
            val flags = source.getSpanFlags(span)

            val newStart = maxOf(0, start)
            val newEnd = minOf(builder.length, end)

            if (newStart < newEnd && newEnd <= builder.length) {
                builder.setSpan(span, newStart, newEnd, flags)
            }
        }
    }

    /**
     * 启动光标闪烁
     */
    private fun startCursorBlink(lifecycleOwner: LifecycleOwner) {
        cursorJob = lifecycleOwner.lifecycleScope.launch {
            var showCursorChar = true

            while (isRendering) {
                delay(CURSOR_BLINK_INTERVAL)

                if (isRendering) {
                    showCursorChar = !showCursorChar
                    cursorChar = if (showCursorChar) "▋" else " "
                }
            }
        }
    }

    /**
     * 停止渲染
     */
    fun stopRender() {
        isRendering = false
        autoScrollManager?.stopAutoScroll()
        renderJob?.cancel()
        cursorJob?.cancel()
        renderJob = null
        cursorJob = null
    }

    /**
     * 是否正在渲染
     */
    fun isRendering(): Boolean = isRendering

    /**
     * 查找包含TextView的ScrollView
     */
    private fun findScrollView(textView: TextView): NestedScrollView? {
        var parent = textView.parent
        while (parent != null) {
            if (parent is NestedScrollView) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    /**
     * 立即完成渲染
     */
    fun completeImmediately(textView: TextView, content: Spanned) {
        stopRender()
        textView.text = content
        onProgressUpdate?.invoke(100, "渲染完成")
    }
}
