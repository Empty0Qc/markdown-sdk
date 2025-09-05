package com.chenge.markdown.compose

import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自动滚动管理器
 * 在Markdown内容流式输出过程中实现智能自动滚动功能
 * 支持用户滚动检测、自动滚动控制和跳转到底部按钮
 */
class AutoScrollManager {

    companion object {
        private const val SCROLL_CHECK_INTERVAL = 150L // 检查间隔（优化：减少检查频率）
        private const val SCROLL_ANIMATION_DURATION = 200L // 滚动动画时长
        private const val BOTTOM_THRESHOLD = 100 // 底部阈值（像素）
        private const val USER_SCROLL_THRESHOLD = 50 // 用户滚动检测阈值（像素）
        private const val AUTO_SCROLL_RESUME_DELAY = 2000L // 自动滚动恢复延迟（毫秒）
        private const val SCROLL_THROTTLE_INTERVAL = 50L // 滚动节流间隔
    }

    private var scrollView: NestedScrollView? = null
    private var targetTextView: TextView? = null
    private var lifecycleOwner: LifecycleOwner? = null

    private var scrollCheckJob: Job? = null
    private var autoScrollResumeJob: Job? = null
    private var isAutoScrollEnabled = true
    private var isAutoScrollActive = true // 当前是否正在自动滚动
    private var isScrolling = false
    private var isUserScrolling = false // 用户是否正在手动滚动
    private var lastScrollY = 0
    private var lastAutoScrollTime = 0L
    private var lastScrollCheckTime = 0L // 上次滚动检查时间
    
    // 性能优化：缓存计算结果
    private var cachedMaxScrollY = -1
    private var cachedTextViewHeight = -1
    private var lastContentHeight = -1

    // 滚动监听器
    private var onScrollListener: ((scrollY: Int, maxScrollY: Int) -> Unit)? = null
    
    // 用户滚动状态变化监听器
    private var onUserScrollStateChanged: ((Boolean) -> Unit)? = null

    /**
     * 初始化自动滚动管理器
     */
    fun initialize(
        scrollView: NestedScrollView,
        textView: TextView,
        lifecycleOwner: LifecycleOwner
    ) {
        this.scrollView = scrollView
        this.targetTextView = textView
        this.lifecycleOwner = lifecycleOwner

        setupScrollListener()
    }

    /**
     * 设置滚动监听器
     */
    private fun setupScrollListener() {
        scrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            onScrollListener?.invoke(scrollY, getMaxScrollY())
            
            // 检测用户滚动行为
            detectUserScroll(scrollY, oldScrollY)
        }
    }
    
    /**
     * 检测用户滚动行为
     */
    private fun detectUserScroll(currentScrollY: Int, previousScrollY: Int) {
        val currentTime = System.currentTimeMillis()
        val scrollDelta = kotlin.math.abs(currentScrollY - previousScrollY)
        
        // 如果滚动变化超过阈值，且不是自动滚动触发的
        if (scrollDelta > USER_SCROLL_THRESHOLD && 
            currentTime - lastAutoScrollTime > SCROLL_ANIMATION_DURATION) {
            
            // 检测用户是否向上滚动（离开底部）
            val maxScrollY = getMaxScrollY()
            val isNearBottom = currentScrollY >= maxScrollY - BOTTOM_THRESHOLD
            
            if (!isNearBottom && !isUserScrolling) {
                // 用户开始手动滚动，暂停自动滚动
                isUserScrolling = true
                isAutoScrollActive = false
                onUserScrollStateChanged?.invoke(true)
                
                // 启动自动恢复定时器
                startAutoScrollResumeTimer()
            } else if (isNearBottom && isUserScrolling) {
                // 用户滚动到底部，恢复自动滚动
                resumeAutoScroll()
            }
        }
        
        lastScrollY = currentScrollY
    }
    
    /**
     * 启动自动滚动恢复定时器
     */
    private fun startAutoScrollResumeTimer() {
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = lifecycleOwner?.lifecycleScope?.launch {
            delay(AUTO_SCROLL_RESUME_DELAY)
            resumeAutoScroll()
        }
    }
    
    /**
     * 恢复自动滚动
     */
    private fun resumeAutoScroll() {
        isUserScrolling = false
        isAutoScrollActive = true
        onUserScrollStateChanged?.invoke(false)
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = null
    }

    /**
     * 强制恢复自动滚动
     * 重置所有状态并立即启动自动滚动
     */
    fun forceResumeAutoScroll() {
        // 重置所有状态
        isUserScrolling = false
        isAutoScrollActive = true
        isAutoScrollEnabled = true
        
        // 取消现有的任务
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = null
        
        // 立即启动自动滚动
        startAutoScroll()
    }

    /**
     * 开始自动滚动监控
     */
    fun startAutoScroll() {
        if (!isAutoScrollEnabled) return

        stopAutoScroll() // 停止之前的任务

        scrollCheckJob = lifecycleOwner?.lifecycleScope?.launch {
            while (isAutoScrollEnabled) {
                // 性能优化：滚动节流
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastScrollCheckTime < SCROLL_THROTTLE_INTERVAL) {
                    delay(SCROLL_CHECK_INTERVAL)
                    continue
                }
                lastScrollCheckTime = currentTime
                
                // 只有在自动滚动激活且用户未手动滚动时才执行
                if (isAutoScrollActive && !isUserScrolling) {
                    checkAndScroll()
                }
                delay(SCROLL_CHECK_INTERVAL)
            }
        }
    }

    /**
     * 停止自动滚动监控
     */
    fun stopAutoScroll() {
        scrollCheckJob?.cancel()
        scrollCheckJob = null
    }

    /**
     * 启用/禁用自动滚动
     */
    fun setAutoScrollEnabled(enabled: Boolean) {
        isAutoScrollEnabled = enabled
        if (!enabled) {
            stopAutoScroll()
        }
    }

    /**
     * 设置滚动监听器
     */
    fun setOnScrollListener(listener: (scrollY: Int, maxScrollY: Int) -> Unit) {
        onScrollListener = listener
    }
    
    /**
     * 设置用户滚动状态变化监听器
     */
    fun setOnUserScrollStateChanged(listener: (Boolean) -> Unit) {
        onUserScrollStateChanged = listener
    }
    
    /**
     * 立即跳转到底部（用于"跳转到底部"按钮）
     */
    fun jumpToBottom() {
        val scrollView = scrollView ?: return
        val maxScrollY = getMaxScrollY()
        
        lastAutoScrollTime = System.currentTimeMillis()
        scrollView.scrollTo(0, maxScrollY) // 立即跳转，不使用动画
        
        // 恢复自动滚动状态
        resumeAutoScroll()
    }
    
    /**
     * 检查是否接近底部
     */
    fun isNearBottom(): Boolean {
        val scrollView = scrollView ?: return false
        val currentScrollY = scrollView.scrollY
        val maxScrollY = getMaxScrollY()
        return currentScrollY >= maxScrollY - BOTTOM_THRESHOLD
    }
    
    /**
     * 获取用户滚动状态
     */
    fun isUserScrolling(): Boolean = isUserScrolling

    /**
     * 检查是否需要滚动并执行滚动
     */
    private suspend fun checkAndScroll() {
        val scrollView = this.scrollView ?: return
        val textView = this.targetTextView ?: return

        if (isScrolling) return // 避免重复滚动

        // 检查内容是否溢出
        if (isContentOverflowing()) {
            performSmoothScroll()
        }
    }

    /**
     * 检查内容是否溢出可视区域
     */
    private fun isContentOverflowing(): Boolean {
        val scrollView = this.scrollView ?: return false
        val textView = this.targetTextView ?: return false

        // 获取TextView的底部位置
        val textViewBottom = getTextViewBottomPosition()

        // 获取ScrollView的可视区域底部
        val scrollViewBottom = scrollView.scrollY + scrollView.height

        // 如果TextView底部超出可视区域底部阈值，则需要滚动
        return textViewBottom > scrollViewBottom - BOTTOM_THRESHOLD
    }

    /**
     * 获取TextView的底部位置
     */
    private fun getTextViewBottomPosition(): Int {
        val textView = this.targetTextView ?: return 0
        val scrollView = this.scrollView ?: return 0

        // 计算TextView相对于ScrollView的位置
        val location = IntArray(2)
        textView.getLocationInWindow(location)
        val textViewY = location[1]

        scrollView.getLocationInWindow(location)
        val scrollViewY = location[1]

        // TextView相对于ScrollView的底部位置
        return textViewY - scrollViewY + textView.height
    }

    /**
     * 执行平滑滚动
     */
    private suspend fun performSmoothScroll() {
        val scrollView = this.scrollView ?: return

        isScrolling = true

        try {
            // 计算目标滚动位置
            val targetScrollY = calculateTargetScrollY()

            // 执行平滑滚动
            scrollView.post {
                scrollView.smoothScrollTo(0, targetScrollY)
            }

            // 等待滚动动画完成
            delay(SCROLL_ANIMATION_DURATION)
        } finally {
            isScrolling = false
        }
    }

    /**
     * 计算目标滚动位置
     */
    private fun calculateTargetScrollY(): Int {
        val scrollView = this.scrollView ?: return 0
        val textView = this.targetTextView ?: return 0

        // 获取TextView的底部位置
        val textViewBottom = getTextViewBottomPosition()

        // 计算需要滚动的距离，使TextView底部保持在可视区域内
        val scrollViewHeight = scrollView.height
        val targetScrollY = textViewBottom - scrollViewHeight + BOTTOM_THRESHOLD

        // 确保不超过最大滚动范围
        val maxScrollY = getMaxScrollY()
        return targetScrollY.coerceAtMost(maxScrollY).coerceAtLeast(0)
    }

    /**
     * 获取最大滚动距离（带缓存优化）
     */
    private fun getMaxScrollY(): Int {
        val scrollView = this.scrollView ?: return 0
        val child = scrollView.getChildAt(0) ?: return 0
        
        val currentContentHeight = child.height
        
        // 性能优化：如果内容高度没有变化，返回缓存的结果
        if (currentContentHeight == lastContentHeight && cachedMaxScrollY != -1) {
            return cachedMaxScrollY
        }
        
        val scrollViewHeight = scrollView.height
        val maxScrollY = (currentContentHeight - scrollViewHeight).coerceAtLeast(0)
        
        // 更新缓存
        lastContentHeight = currentContentHeight
        cachedMaxScrollY = maxScrollY
        cachedTextViewHeight = currentContentHeight
        
        return maxScrollY
    }

    /**
     * 立即滚动到底部
     */
    fun scrollToBottom() {
        val scrollView = this.scrollView ?: return
        val maxScrollY = getMaxScrollY()

        lastAutoScrollTime = System.currentTimeMillis() // 记录自动滚动时间
        scrollView.post {
            scrollView.smoothScrollTo(0, maxScrollY)
        }
    }

    /**
     * 立即滚动到顶部
     */
    fun scrollToTop() {
        val scrollView = this.scrollView ?: return

        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }
    }

    /**
     * 获取当前滚动位置百分比
     */
    fun getScrollPercentage(): Float {
        val scrollView = this.scrollView ?: return 0f
        val maxScrollY = getMaxScrollY()

        return if (maxScrollY > 0) {
            (scrollView.scrollY.toFloat() / maxScrollY).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        scrollCheckJob?.cancel()
        scrollCheckJob = null
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = null
        
        scrollView = null
        targetTextView = null
        lifecycleOwner = null
        onScrollListener = null
        onUserScrollStateChanged = null
        
        // 清理缓存变量
        cachedMaxScrollY = -1
        cachedTextViewHeight = -1
        lastContentHeight = -1
        lastScrollCheckTime = 0L
        lastAutoScrollTime = 0L
        lastScrollY = 0
        
        // 重置状态
        isAutoScrollEnabled = true
        isAutoScrollActive = true
        isScrolling = false
        isUserScrolling = false
    }
}
