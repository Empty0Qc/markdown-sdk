package com.chenge.markdown.engine

import android.content.Context
import android.widget.TextView
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * TextView 回收池
 * 在列表场景中复用 TextView，避免频繁创建和销毁
 */
class TextViewPool private constructor() {
    
    companion object {
        private const val MAX_POOL_SIZE = 20
        private const val INITIAL_POOL_SIZE = 5
        
        @Volatile
        private var INSTANCE: TextViewPool? = null
        
        fun getInstance(): TextViewPool {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TextViewPool().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * TextView 池
     */
    private val textViewPool = ConcurrentLinkedQueue<TextView>()
    
    /**
     * 当前池大小
     */
    private val poolSize = AtomicInteger(0)
    
    /**
     * 统计信息
     */
    private val hitCount = AtomicInteger(0)
    private val missCount = AtomicInteger(0)
    private val recycleCount = AtomicInteger(0)
    
    /**
     * 从池中获取 TextView
     */
    fun acquire(context: Context): TextView {
        val textView = textViewPool.poll()
        
        return if (textView != null) {
            poolSize.decrementAndGet()
            hitCount.incrementAndGet()
            
            // 重置 TextView 状态
            resetTextView(textView)
            textView
        } else {
            missCount.incrementAndGet()
            createNewTextView(context)
        }
    }
    
    /**
     * 回收 TextView 到池中
     */
    fun release(textView: TextView) {
        if (poolSize.get() < MAX_POOL_SIZE) {
            // 清理 TextView 状态
            cleanupTextView(textView)
            
            textViewPool.offer(textView)
            poolSize.incrementAndGet()
            recycleCount.incrementAndGet()
        }
    }
    
    /**
     * 预热池（创建初始 TextView）
     */
    fun warmUp(context: Context) {
        MarkdownScheduler.executeRender {
            repeat(INITIAL_POOL_SIZE) {
                if (poolSize.get() < INITIAL_POOL_SIZE) {
                    val textView = createNewTextView(context)
                    release(textView)
                }
            }
        }
    }
    
    /**
     * 创建新的 TextView
     */
    private fun createNewTextView(context: Context): TextView {
        return TextView(context).apply {
            // 设置默认属性
            textSize = 14f
            setPadding(16, 8, 16, 8)
            
            // 启用文本选择
            setTextIsSelectable(true)
            
            // 设置行间距
            setLineSpacing(4f, 1.2f)
        }
    }
    
    /**
     * 重置 TextView 状态
     */
    private fun resetTextView(textView: TextView) {
        textView.apply {
            // 清空文本
            text = ""
            
            // 重置可见性
            visibility = TextView.VISIBLE
            
            // 重置点击监听器
            setOnClickListener(null)
            setOnLongClickListener(null)
            
            // 重置移动方法
            movementMethod = null
            
            // 重置标签
            tag = null
        }
    }
    
    /**
     * 清理 TextView 状态
     */
    private fun cleanupTextView(textView: TextView) {
        textView.apply {
            // 清空文本和样式
            text = ""
            
            // 移除回调
            setOnClickListener(null)
            setOnLongClickListener(null)
            movementMethod = null
            
            // 清理标签
            tag = null
            
            // 重置变换
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            rotation = 0f
            translationX = 0f
            translationY = 0f
        }
    }
    
    /**
     * 清空池
     */
    fun clear() {
        textViewPool.clear()
        poolSize.set(0)
    }
    
    /**
     * 获取池统计信息
     */
    fun getStats(): PoolStats {
        val total = hitCount.get() + missCount.get()
        val hitRate = if (total > 0) hitCount.get().toFloat() / total else 0f
        
        return PoolStats(
            currentSize = poolSize.get(),
            maxSize = MAX_POOL_SIZE,
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            recycleCount = recycleCount.get(),
            hitRate = hitRate
        )
    }
    
    /**
     * 重置统计信息
     */
    fun resetStats() {
        hitCount.set(0)
        missCount.set(0)
        recycleCount.set(0)
    }
    
    data class PoolStats(
        val currentSize: Int,
        val maxSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val recycleCount: Int,
        val hitRate: Float
    )
}

/**
 * TextView 池扩展函数
 */
fun TextView.recycleToPool() {
    TextViewPool.getInstance().release(this)
}

/**
 * Context 扩展函数，从池中获取 TextView
 */
fun Context.acquireTextView(): TextView {
    return TextViewPool.getInstance().acquire(this)
}