package com.chenge.markdownsdk

import android.util.Log
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
        private const val TAG = "AutoScrollManager"
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
    private var onUserScrollStateChanged: ((isUserScrolling: Boolean) -> Unit)? = null

    /**
     * 初始化自动滚动管理器
     */
    fun initialize(
        scrollView: NestedScrollView,
        textView: TextView,
        lifecycleOwner: LifecycleOwner
    ) {
        Log.d(TAG, "Initializing AutoScrollManager")
        this.scrollView = scrollView
        this.targetTextView = textView
        this.lifecycleOwner = lifecycleOwner

        setupScrollListener()
        Log.d(TAG, "AutoScrollManager initialized successfully")
    }

    /**
     * 设置滚动监听器
     */
    private fun setupScrollListener() {
        scrollView?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val maxScrollY = getMaxScrollY()
            onScrollListener?.invoke(scrollY, maxScrollY)
            
            // 检测用户滚动行为
            detectUserScroll(scrollY, oldScrollY)
        }
    }
    
    /**
     * 检测用户滚动行为
     */
    private fun detectUserScroll(currentScrollY: Int, previousScrollY: Int) {
        val scrollDelta = kotlin.math.abs(currentScrollY - previousScrollY)
        val currentTime = System.currentTimeMillis()
        
        // 如果滚动变化超过阈值且不是自动滚动引起的
        if (scrollDelta > USER_SCROLL_THRESHOLD && 
            currentTime - lastAutoScrollTime > SCROLL_ANIMATION_DURATION) {
            
            // 检测向上滚动（用户主动滚动）
            if (currentScrollY < previousScrollY) {
                if (!isUserScrolling) {
                    isUserScrolling = true
                    isAutoScrollActive = false
                    onUserScrollStateChanged?.invoke(true)
                    
                    // 取消自动滚动恢复任务
                    autoScrollResumeJob?.cancel()
                }
            }
            // 检测向下滚动到底部附近（可能要恢复自动滚动）
            else if (isNearBottom(currentScrollY)) {
                if (isUserScrolling) {
                    scheduleAutoScrollResume()
                }
            }
        }
        
        lastScrollY = currentScrollY
    }
    
    /**
     * 检查是否接近底部
     */
    private fun isNearBottom(scrollY: Int): Boolean {
        val maxScrollY = getMaxScrollY()
        return scrollY >= maxScrollY - BOTTOM_THRESHOLD
    }
    
    /**
     * 安排自动滚动恢复
     */
    private fun scheduleAutoScrollResume() {
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = lifecycleOwner?.lifecycleScope?.launch {
            delay(AUTO_SCROLL_RESUME_DELAY)
            if (isUserScrolling && isNearBottom(lastScrollY)) {
                resumeAutoScroll()
            }
        }
    }
    
    /**
     * 恢复自动滚动
     */
    private fun resumeAutoScroll() {
        isUserScrolling = false
        isAutoScrollActive = true
        onUserScrollStateChanged?.invoke(false)
    }

    /**
     * 开始自动滚动监控
     */
    fun startAutoScroll() {
        Log.d(TAG, "Starting auto scroll, enabled: $isAutoScrollEnabled")
        if (!isAutoScrollEnabled) return

        stopAutoScroll() // 停止之前的任务

        scrollCheckJob = lifecycleOwner?.lifecycleScope?.launch {
            Log.d(TAG, "Auto scroll job started")
            while (isAutoScrollEnabled) {
                checkAndScroll()
                delay(SCROLL_CHECK_INTERVAL)
            }
            Log.d(TAG, "Auto scroll job ended")
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
     * 设置滚动监听回调
     */
    fun setOnScrollListener(listener: (scrollY: Int, maxScrollY: Int) -> Unit) {
        onScrollListener = listener
    }
    
    /**
     * 设置用户滚动状态变化监听器
     */
    fun setOnUserScrollStateChangedListener(listener: (isUserScrolling: Boolean) -> Unit) {
        onUserScrollStateChanged = listener
    }
    
    /**
     * 获取当前是否为用户滚动状态
     */
    fun isUserScrolling(): Boolean {
        return isUserScrolling
    }
    
    /**
     * 手动跳转到底部（用户点击按钮时调用）
     */
    fun jumpToBottom() {
        scrollToBottom()
        // 恢复自动滚动状态
        resumeAutoScroll()
    }
    
    /**
     * 强制恢复自动滚动（用于特殊情况）
     */
    fun forceResumeAutoScroll() {
        Log.d(TAG, "Force resuming auto scroll")
        isUserScrolling = false
        isAutoScrollActive = true
        isAutoScrollEnabled = true
        onUserScrollStateChanged?.invoke(false)
        
        // 取消现有的任务
        autoScrollResumeJob?.cancel()
        
        // 立即启动自动滚动
        startAutoScroll()
        Log.d(TAG, "Auto scroll force resumed - enabled: $isAutoScrollEnabled, active: $isAutoScrollActive, userScrolling: $isUserScrolling")
    }

    /**
     * 检查是否需要滚动并执行滚动
     */
    private suspend fun checkAndScroll() {
        val currentTime = System.currentTimeMillis()
        
        // 节流：如果距离上次检查时间太短，跳过本次检查
        if (currentTime - lastScrollCheckTime < SCROLL_THROTTLE_INTERVAL) {
            return
        }
        lastScrollCheckTime = currentTime
        
        this.scrollView ?: return
        this.targetTextView ?: return

        if (isScrolling || !isAutoScrollActive || isUserScrolling) {
            Log.d(TAG, "Skipping scroll - isScrolling: $isScrolling, isAutoScrollActive: $isAutoScrollActive, isUserScrolling: $isUserScrolling")
            return // 避免重复滚动和用户滚动冲突
        }

        try {
            // 检查内容是否溢出
            if (isContentOverflowing()) {
                Log.d(TAG, "Content overflowing, performing smooth scroll")
                performSmoothScroll()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndScroll", e)
            // 忽略异常，避免崩溃
        }
    }

    /**
     * 检查内容是否溢出可视区域
     */
    private fun isContentOverflowing(): Boolean {
        val scrollView = this.scrollView ?: return false
        this.targetTextView ?: return false

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
        lastAutoScrollTime = System.currentTimeMillis()

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
        this.targetTextView ?: return 0

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
        val currentScrollViewHeight = scrollView.height
        
        // 如果内容高度或滚动视图高度发生变化，重新计算并缓存
        if (lastContentHeight != currentContentHeight || cachedTextViewHeight != currentScrollViewHeight) {
            lastContentHeight = currentContentHeight
            cachedTextViewHeight = currentScrollViewHeight
            cachedMaxScrollY = maxOf(0, currentContentHeight - currentScrollViewHeight)
        }
        
        return cachedMaxScrollY
    }

    /**
     * 立即滚动到底部
     */
    fun scrollToBottom() {
        val scrollView = this.scrollView ?: return
        val maxScrollY = getMaxScrollY()

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
        stopAutoScroll()
        autoScrollResumeJob?.cancel()
        autoScrollResumeJob = null
        onUserScrollStateChanged = null
        isUserScrolling = false
        isAutoScrollActive = true
        
        // 清理缓存
        cachedMaxScrollY = -1
        cachedTextViewHeight = -1
        lastContentHeight = -1
        lastScrollCheckTime = 0L
        
        scrollView = null
        targetTextView = null
        lifecycleOwner = null
        onScrollListener = null
    }
}
