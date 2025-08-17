package com.chenge.markdown.engine

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * 图片缓存管理器
 * 提供内存缓存和磁盘缓存，优化 Markdown 中图片的加载性能
 */
class ImageCacheManager private constructor(private val context: Context) {

    companion object {
        private const val MEMORY_CACHE_SIZE = 1024 * 1024 * 10 // 10MB
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 50L // 50MB
        private const val CACHE_DIR_NAME = "markdown_image_cache"

        @Volatile
        private var INSTANCE: ImageCacheManager? = null

        fun getInstance(context: Context): ImageCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * 内存缓存
     */
    private val memoryCache = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted && !oldValue.isRecycled) {
                // 可选：回收 Bitmap（谨慎使用）
                // oldValue.recycle()
            }
        }
    }

    /**
     * 磁盘缓存目录
     */
    private val diskCacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * 正在加载的图片 URL 集合（避免重复加载）
     */
    private val loadingUrls = ConcurrentHashMap<String, Boolean>()

    /**
     * 从缓存获取图片
     */
    fun getBitmap(url: String): Bitmap? {
        val key = generateCacheKey(url)

        // 先从内存缓存获取
        memoryCache.get(key)?.let { return it }

        // 再从磁盘缓存获取
        return getDiskCachedBitmap(key)
    }

    /**
     * 缓存图片
     */
    fun putBitmap(url: String, bitmap: Bitmap) {
        val key = generateCacheKey(url)

        // 存入内存缓存
        memoryCache.put(key, bitmap)

        // 异步存入磁盘缓存
        MarkdownScheduler.executeImage {
            saveBitmapToDisk(key, bitmap)
        }
    }

    /**
     * 检查是否正在加载
     */
    fun isLoading(url: String): Boolean {
        return loadingUrls.containsKey(url)
    }

    /**
     * 标记开始加载
     */
    fun markLoading(url: String) {
        loadingUrls[url] = true
    }

    /**
     * 标记加载完成
     */
    fun markLoadingComplete(url: String) {
        loadingUrls.remove(url)
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(url: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(url.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            url.hashCode().toString()
        }
    }

    /**
     * 从磁盘缓存获取图片
     */
    private fun getDiskCachedBitmap(key: String): Bitmap? {
        return try {
            val file = File(diskCacheDir, key)
            if (file.exists() && file.length() > 0) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存图片到磁盘缓存
     */
    private fun saveBitmapToDisk(key: String, bitmap: Bitmap) {
        try {
            val file = File(diskCacheDir, key)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            // 忽略磁盘缓存错误
        }
    }

    /**
     * 清理过期缓存
     */
    fun cleanupExpired() {
        MarkdownScheduler.executeImage {
            try {
                val now = System.currentTimeMillis()
                val expireTime = 7 * 24 * 60 * 60 * 1000L // 7天

                diskCacheDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > expireTime) {
                        file.delete()
                    }
                }

                // 检查磁盘缓存大小
                val totalSize = diskCacheDir.listFiles()?.sumOf { it.length() } ?: 0
                if (totalSize > DISK_CACHE_SIZE) {
                    // 删除最旧的文件直到大小合适
                    val files = diskCacheDir.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
                    var currentSize = totalSize

                    for (file in files) {
                        if (currentSize <= DISK_CACHE_SIZE * 0.8) break
                        currentSize -= file.length()
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                // 忽略清理错误
            }
        }
    }

    /**
     * 清空所有缓存
     */
    fun clearAll() {
        // 清空内存缓存
        memoryCache.evictAll()

        // 清空磁盘缓存
        MarkdownScheduler.executeImage {
            try {
                diskCacheDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                // 忽略清理错误
            }
        }

        // 清空加载状态
        loadingUrls.clear()
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        val memorySize = memoryCache.size()
        val memoryMaxSize = memoryCache.maxSize()
        val diskSize = try {
            diskCacheDir.listFiles()?.sumOf { it.length() } ?: 0
        } catch (e: Exception) {
            0
        }
        val diskFileCount = try {
            diskCacheDir.listFiles()?.size ?: 0
        } catch (e: Exception) {
            0
        }

        return CacheStats(
            memorySize = memorySize,
            memoryMaxSize = memoryMaxSize,
            diskSize = diskSize,
            diskFileCount = diskFileCount,
            loadingCount = loadingUrls.size
        )
    }

    data class CacheStats(
        val memorySize: Int,
        val memoryMaxSize: Int,
        val diskSize: Long,
        val diskFileCount: Int,
        val loadingCount: Int
    )
}
