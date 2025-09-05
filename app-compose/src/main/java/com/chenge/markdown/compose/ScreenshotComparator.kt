package com.chenge.markdown.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 截图对比工具类
 * 支持自动化视觉回归测试
 */
class ScreenshotComparator(private val context: Context) {

    companion object {
        private const val TAG = "ScreenshotComparator"
        private const val SCREENSHOT_DIR = "screenshots"
        private const val BASELINE_DIR = "baseline"
        private const val DIFF_DIR = "diff"
        
        // 对比阈值
        private const val PIXEL_TOLERANCE = 10 // 像素差异容忍度
        private const val DIFF_PERCENTAGE_THRESHOLD = 0.05f // 5% 差异阈值
    }

    private val screenshotDir: File
    private val baselineDir: File
    private val diffDir: File

    init {
        val baseDir = File(context.filesDir, SCREENSHOT_DIR)
        baseDir.mkdirs()
        
        screenshotDir = File(baseDir, "current")
        baselineDir = File(baseDir, BASELINE_DIR)
        diffDir = File(baseDir, DIFF_DIR)
        
        screenshotDir.mkdirs()
        baselineDir.mkdirs()
        diffDir.mkdirs()
    }

    /**
     * 截图对比结果
     */
    data class ComparisonResult(
        val isMatch: Boolean,
        val diffPercentage: Float,
        val diffPixelCount: Int,
        val totalPixelCount: Int,
        val diffImagePath: String? = null,
        val error: String? = null
    )

    /**
     * 捕获指定View的截图
     */
    fun captureView(view: View, filename: String): Bitmap? {
        return try {
            val width = view.width
            val height = view.height
            
            if (width <= 0 || height <= 0) {
                Log.w(TAG, "View dimensions invalid: ${width}x${height}")
                return null
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            
            // 保存截图到文件
            saveScreenshot(bitmap, filename)
            
            Log.d(TAG, "Screenshot captured: $filename (${width}x${height})")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture view screenshot", e)
            null
        }
    }

    /**
     * 保存截图到文件
     */
    private fun saveScreenshot(bitmap: Bitmap, filename: String) {
        val file = File(screenshotDir, "$filename.png")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Screenshot saved: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save screenshot", e)
        }
    }

    /**
     * 对比当前截图与基准截图
     */
    fun compareWithBaseline(filename: String): ComparisonResult {
        val currentFile = File(screenshotDir, "$filename.png")
        val baselineFile = File(baselineDir, "$filename.png")

        if (!currentFile.exists()) {
            return ComparisonResult(
                isMatch = false,
                diffPercentage = 100f,
                diffPixelCount = 0,
                totalPixelCount = 0,
                error = "Current screenshot not found: ${currentFile.absolutePath}"
            )
        }

        if (!baselineFile.exists()) {
            // 如果基准图片不存在，将当前截图复制为基准
            try {
                currentFile.copyTo(baselineFile, overwrite = true)
                Log.i(TAG, "Created new baseline: $filename")
                return ComparisonResult(
                    isMatch = true,
                    diffPercentage = 0f,
                    diffPixelCount = 0,
                    totalPixelCount = 0
                )
            } catch (e: Exception) {
                return ComparisonResult(
                    isMatch = false,
                    diffPercentage = 100f,
                    diffPixelCount = 0,
                    totalPixelCount = 0,
                    error = "Failed to create baseline: ${e.message}"
                )
            }
        }

        return try {
            val currentBitmap = android.graphics.BitmapFactory.decodeFile(currentFile.absolutePath)
            val baselineBitmap = android.graphics.BitmapFactory.decodeFile(baselineFile.absolutePath)
            
            if (currentBitmap == null || baselineBitmap == null) {
                return ComparisonResult(
                    isMatch = false,
                    diffPercentage = 100f,
                    diffPixelCount = 0,
                    totalPixelCount = 0,
                    error = "Failed to decode images"
                )
            }

            compareImages(currentBitmap, baselineBitmap, filename)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compare images", e)
            ComparisonResult(
                isMatch = false,
                diffPercentage = 100f,
                diffPixelCount = 0,
                totalPixelCount = 0,
                error = "Comparison failed: ${e.message}"
            )
        }
    }

    /**
     * 对比两张图片
     */
    private fun compareImages(current: Bitmap, baseline: Bitmap, filename: String): ComparisonResult {
        // 检查尺寸是否匹配
        if (current.width != baseline.width || current.height != baseline.height) {
            return ComparisonResult(
                isMatch = false,
                diffPercentage = 100f,
                diffPixelCount = 0,
                totalPixelCount = current.width * current.height,
                error = "Image dimensions mismatch: current(${current.width}x${current.height}) vs baseline(${baseline.width}x${baseline.height})"
            )
        }

        val width = current.width
        val height = current.height
        val totalPixels = width * height
        var diffPixels = 0

        // 创建差异图片
        val diffBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val currentPixel = current.getPixel(x, y)
                val baselinePixel = baseline.getPixel(x, y)

                if (isPixelDifferent(currentPixel, baselinePixel)) {
                    diffPixels++
                    // 用红色标记差异像素
                    diffBitmap.setPixel(x, y, Color.RED)
                } else {
                    // 保持原始像素，但降低透明度
                    val alpha = Color.alpha(currentPixel) / 4
                    val red = Color.red(currentPixel)
                    val green = Color.green(currentPixel)
                    val blue = Color.blue(currentPixel)
                    diffBitmap.setPixel(x, y, Color.argb(alpha, red, green, blue))
                }
            }
        }

        val diffPercentage = (diffPixels.toFloat() / totalPixels) * 100f
        val isMatch = diffPercentage <= DIFF_PERCENTAGE_THRESHOLD * 100f

        // 保存差异图片
        val diffPath = if (diffPixels > 0) {
            val diffFile = File(diffDir, "$filename.png")
            try {
                FileOutputStream(diffFile).use { out ->
                    diffBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                diffFile.absolutePath
            } catch (e: IOException) {
                Log.e(TAG, "Failed to save diff image", e)
                null
            }
        } else null

        Log.d(TAG, "Comparison result for $filename: " +
                "match=$isMatch, diffPixels=$diffPixels/$totalPixels (${String.format("%.2f", diffPercentage)}%)")

        return ComparisonResult(
            isMatch = isMatch,
            diffPercentage = diffPercentage,
            diffPixelCount = diffPixels,
            totalPixelCount = totalPixels,
            diffImagePath = diffPath
        )
    }

    /**
     * 检查两个像素是否不同
     */
    private fun isPixelDifferent(pixel1: Int, pixel2: Int): Boolean {
        val alpha1 = Color.alpha(pixel1)
        val red1 = Color.red(pixel1)
        val green1 = Color.green(pixel1)
        val blue1 = Color.blue(pixel1)

        val alpha2 = Color.alpha(pixel2)
        val red2 = Color.red(pixel2)
        val green2 = Color.green(pixel2)
        val blue2 = Color.blue(pixel2)

        return abs(alpha1 - alpha2) > PIXEL_TOLERANCE ||
                abs(red1 - red2) > PIXEL_TOLERANCE ||
                abs(green1 - green2) > PIXEL_TOLERANCE ||
                abs(blue1 - blue2) > PIXEL_TOLERANCE
    }

    /**
     * 更新基准截图
     */
    fun updateBaseline(filename: String): Boolean {
        val currentFile = File(screenshotDir, "$filename.png")
        val baselineFile = File(baselineDir, "$filename.png")

        return try {
            if (currentFile.exists()) {
                currentFile.copyTo(baselineFile, overwrite = true)
                Log.i(TAG, "Updated baseline: $filename")
                true
            } else {
                Log.w(TAG, "Current screenshot not found for baseline update: $filename")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update baseline: $filename", e)
            false
        }
    }

    /**
     * 清理旧的截图文件
     */
    fun cleanupOldScreenshots(keepDays: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
        
        listOf(screenshotDir, diffDir).forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted old screenshot: ${file.name}")
                    }
                }
            }
        }
    }

    /**
     * 获取目录信息
     */
    fun getDirectoryInfo(): Map<String, Any> {
        return mapOf(
            "screenshotDir" to screenshotDir.absolutePath,
            "baselineDir" to baselineDir.absolutePath,
            "diffDir" to diffDir.absolutePath,
            "currentCount" to (screenshotDir.listFiles()?.size ?: 0),
            "baselineCount" to (baselineDir.listFiles()?.size ?: 0),
            "diffCount" to (diffDir.listFiles()?.size ?: 0)
        )
    }
}