package com.chenge.markdown.sample

import android.os.Bundle
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.MarkdownSanitizer
import com.chenge.markdown.plugins.MarkdownPlugins
import com.chenge.markdown.render.MarkdownView
import com.chenge.markdown.debug.MarkdownDebugRenderer

class MainActivity : AppCompatActivity() {

  private var isSyncMode = true // 初始为同步模式

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val markdownView = findViewById<MarkdownView>(R.id.markdownView)
    val fabTiming = findViewById<ExtendedFloatingActionButton>(R.id.fabTiming)
    val fabDebug = findViewById<ExtendedFloatingActionButton>(R.id.fabDebug)

    val rawMarkdown = """
            # 性能测试

            ${"- 列表\n".repeat(500)}
        """.trimIndent()

    val safeMarkdown = MarkdownSanitizer.sanitize(rawMarkdown)

    val config = MarkdownConfig(
      enableHtml = false,
      enableTables = true,
      enableTaskList = true
    )

    val markwon = MarkdownPlugins.create(this, config)

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
          markwon,
          markdownView,
          safeMarkdown
        ) { duration ->
          fabTiming.text = "同步耗时 ${duration}ms"
          fabTiming.extend()
        }
      } else {
        // 异步渲染
        MarkdownDebugRenderer.setMarkdownAsyncWithTiming(
          markwon,
          markdownView,
          safeMarkdown
        ) { duration ->
          fabTiming.text = "异步耗时 ${duration}ms"
          fabTiming.extend()
        }
      }
      // 切换模式
      isSyncMode = !isSyncMode
    }
  }
}
