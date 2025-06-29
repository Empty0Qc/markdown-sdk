package com.chenge.markdown.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.MarkdownSanitizer
import com.chenge.markdown.core.EmojiReplacer
import com.chenge.markdown.debug.MarkdownDebugRenderer
import com.chenge.markdown.plugins.ClickablePlugin
import com.chenge.markdown.plugins.ImageSizePlugin
import com.chenge.markdown.plugins.MarkdownPlugins
import com.chenge.markdown.render.MarkdownView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

  private var isSyncMode = true // 初始为同步模式

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val markdownView = findViewById<MarkdownView>(R.id.markdownView)
    val fabTiming = findViewById<ExtendedFloatingActionButton>(R.id.fabTiming)
    val fabDebug = findViewById<ExtendedFloatingActionButton>(R.id.fabDebug)

    // 加载 assets 文件里的全格式 Markdown 内容
    val rawMarkdown = loadMarkdownFromAssets("full_format_test.md")
    // Emoji 替换
    val markdownWithEmoji = EmojiReplacer.replaceShortcodes(rawMarkdown)

    val safeMarkdown = MarkdownSanitizer.sanitize(markdownWithEmoji)
    val config = MarkdownConfig(
      enableHtml = true, enableTables = true, enableTaskList = true
    )

    // 图片解析
    MarkdownPlugins.register(ImageSizePlugin(this))
    // 长按点击
    MarkdownPlugins.register(
      ClickablePlugin(onLinkClick = { url ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
      }, onCodeClick = { code ->
        Log.d("Clickable", "点击代码: $code")
      })
    )
    // 调整样式配置
//    MarkdownPlugins.register(
//      StylePlugin(MarkdownStyleConfig(headingColor = Color.RED))
//    )

    val markwon = MarkdownPlugins.create(this, config)
    markdownView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

    // 耗时按钮默认收起
    fabTiming.shrink()
    fabTiming.setOnClickListener {
      if (fabTiming.isExtended) fabTiming.shrink() else fabTiming.extend()
    }

    // 点击调试按钮
    fabDebug.setOnClickListener {
      if (isSyncMode) {
        // 同步渲染
        MarkdownDebugRenderer.setMarkdownSyncWithTiming(
          markwon, markdownView, safeMarkdown
        ) { duration ->
          fabTiming.text = "同步耗时 ${duration}ms"
          fabTiming.extend()
          Log.d("MarkdownDebug", "同步渲染完成，耗时: ${duration}ms")
        }
      } else {
        // 异步渲染
        MarkdownDebugRenderer.setMarkdownAsyncWithTiming(
          markwon, markdownView, safeMarkdown
        ) { duration ->
          fabTiming.text = "异步耗时 ${duration}ms"
          fabTiming.extend()
          Log.d("MarkdownDebug", "异步渲染完成，耗时: ${duration}ms")
        }
      }
      // 切换模式
      isSyncMode = !isSyncMode
    }
  }

  private fun loadMarkdownFromAssets(fileName: String): String {
    return try {
      assets.open(fileName).use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).readText()
      }
    } catch (e: Exception) {
      Log.e("MarkdownLoad", "加载 Markdown 文件失败: ${e.message}")
      "# 加载失败\n无法读取文件内容。"
    }
  }
}
