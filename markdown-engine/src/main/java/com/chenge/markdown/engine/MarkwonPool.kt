package com.chenge.markdown.engine

import android.content.Context
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.plugins.MarkdownPlugins
import io.noties.markwon.Markwon
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Markwon 实例池，用于复用 Markwon 实例以提高性能
 * 避免重复创建相同配置的 Markwon 实例
 */
object MarkwonPool {
    
    private const val MAX_POOL_SIZE = 5
    private val instancePool = ConcurrentHashMap<String, PooledMarkwon>()
    private val poolSize = AtomicInteger(0)
    
    /**
     * 池化的 Markwon 实例
     */
    private data class PooledMarkwon(
        val markwon: Markwon,
        val config: MarkdownConfig,
        @Volatile var lastUsed: Long = System.currentTimeMillis(),
        @Volatile var useCount: Int = 0
    )
    
    /**
     * 获取或创建 Markwon 实例
     */
    fun getOrCreate(context: Context, config: MarkdownConfig): Markwon {
        val configKey = generateConfigKey(config)
        
        // 尝试从池中获取现有实例
        val pooled = instancePool[configKey]
        if (pooled != null) {
            pooled.lastUsed = System.currentTimeMillis()
            pooled.useCount++
            return pooled.markwon
        }
        
        // 创建新实例
        val markwon = MarkdownPlugins.create(context, config)
        val newPooled = PooledMarkwon(markwon, config)
        
        // 如果池未满，添加到池中
        if (poolSize.get() < MAX_POOL_SIZE) {
            instancePool[configKey] = newPooled
            poolSize.incrementAndGet()
        } else {
            // 池已满，移除最久未使用的实例
            evictLeastRecentlyUsed()
            instancePool[configKey] = newPooled
        }
        
        return markwon
    }
    
    /**
     * 生成配置的唯一键
     */
    private fun generateConfigKey(config: MarkdownConfig): String {
        return buildString {
            append("html:").append(config.enableHtml)
            append("|tables:").append(config.enableTables)
            append("|tasks:").append(config.enableTaskList)
            append("|latex:").append(config.enableLatex)
            append("|img:").append(config.enableImageLoading)
            append("|click:").append(config.enableLinkClick)
            append("|highlight:").append(config.codeHighlight)
            append("|async:").append(config.asyncRendering)
            append("|debug:").append(config.debugMode)
            append("|maxW:").append(config.maxImageWidth)
            append("|maxH:").append(config.maxImageHeight)
            append("|plugins:").append(config.customPlugins.joinToString(","))
        }
    }
    
    /**
     * 移除最久未使用的实例
     */
    private fun evictLeastRecentlyUsed() {
        val lruEntry = instancePool.entries.minByOrNull { it.value.lastUsed }
        lruEntry?.let {
            instancePool.remove(it.key)
            poolSize.decrementAndGet()
        }
    }
    
    /**
     * 清理过期的实例（超过 5 分钟未使用）
     */
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val expiredThreshold = 5 * 60 * 1000L // 5 分钟
        
        val expiredKeys = instancePool.entries
            .filter { now - it.value.lastUsed > expiredThreshold }
            .map { it.key }
        
        expiredKeys.forEach {
            instancePool.remove(it)
            poolSize.decrementAndGet()
        }
    }
    
    /**
     * 获取池的统计信息
     */
    fun getPoolStats(): PoolStats {
        return PoolStats(
            size = poolSize.get(),
            maxSize = MAX_POOL_SIZE,
            instances = instancePool.values.map { 
                InstanceStats(
                    useCount = it.useCount,
                    lastUsed = it.lastUsed,
                    config = it.config
                )
            }
        )
    }
    
    /**
     * 清空池
     */
    fun clear() {
        instancePool.clear()
        poolSize.set(0)
    }
    
    data class PoolStats(
        val size: Int,
        val maxSize: Int,
        val instances: List<InstanceStats>
    )
    
    data class InstanceStats(
        val useCount: Int,
        val lastUsed: Long,
        val config: MarkdownConfig
    )
}