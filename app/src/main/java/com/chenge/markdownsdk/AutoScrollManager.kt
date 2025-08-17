package com.chenge.markdownsdk

import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自动滚动管理器
 * 在Markdown内容流式输出过程中实现自动滚动功能
 */
class AutoScrollManager {
    
    companion object {
        private const val SCROLL_CHECK_INTERVAL = 100L // 检查间隔
        private const val SCROLL_ANIMATION_DURATION = 200L // 滚动动画时长
        private const val BOTTOM_THRESHOLD = 100 // 底部阈值（像素）
    }
    
    private var scrollView: NestedScrollView? = null
    private var targetTextView: TextView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    
    private var scrollCheckJob: Job? = null
    private var isAutoScrollEnabled = true
    private var isScrolling = false
    
    // 滚动监听器
    private var onScrollListener: ((scrollY: Int, maxScrollY: Int) -> Unit)? = null
    
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
        scrollView?.viewTreeObserver?.addOnScrollChangedListener {
            val scrollY = scrollView?.scrollY ?: 0
            val maxScrollY = getMaxScrollY()
            onScrollListener?.invoke(scrollY, maxScrollY)
        }
    }
    
    /**
     * 开始自动滚动监控
     */
    fun startAutoScroll() {
        if (!isAutoScrollEnabled) return
        
        stopAutoScroll() // 停止之前的任务
        
        scrollCheckJob = lifecycleOwner?.lifecycleScope?.launch {
            while (isAutoScrollEnabled) {
                checkAndScroll()
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
     * 设置滚动监听回调
     */
    fun setOnScrollListener(listener: (scrollY: Int, maxScrollY: Int) -> Unit) {
        onScrollListener = listener
    }
    
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
     * 获取最大滚动距离
     */
    private fun getMaxScrollY(): Int {
        val scrollView = this.scrollView ?: return 0
        val child = scrollView.getChildAt(0) ?: return 0
        return (child.height - scrollView.height).coerceAtLeast(0)
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
        scrollView = null
        targetTextView = null
        lifecycleOwner = null
        onScrollListener = null
    }
}