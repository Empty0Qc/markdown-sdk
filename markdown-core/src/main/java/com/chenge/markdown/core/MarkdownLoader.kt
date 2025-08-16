package com.chenge.markdown.core

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility to load markdown files from assets.
 */
object MarkdownLoader {
    fun loadFromAssets(context: Context, fileName: String): String {
        return try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            }
        } catch (e: Exception) {
            Log.e("MarkdownLoader", "Failed to load markdown: ${e.message}")
            "# 加载失败\n无法读取文件内容。"
        }
    }
}
