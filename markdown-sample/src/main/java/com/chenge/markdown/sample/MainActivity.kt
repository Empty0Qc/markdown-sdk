package com.chenge.markdown.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.markdownConfig
import com.chenge.markdown.engine.MarkdownLoader
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.engine.MarkdownParser
import com.chenge.markdown.engine.ClickablePlugin
import com.chenge.markdown.engine.ImageSizePlugin
import com.chenge.markdown.engine.MarkdownPlugins
import com.chenge.markdown.engine.MarkdownView
import com.chenge.markdown.debug.MarkdownDebugRenderer
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import android.text.method.LinkMovementMethod

class MainActivity : AppCompatActivity() {

  private var isSyncMode = true // 初始为同步模式

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val markdownView = findViewById<MarkdownView>(R.id.markdownView)
    val fabTiming = findViewById<ExtendedFloatingActionButton>(R.id.fabTiming)
    val fabDebug = findViewById<ExtendedFloatingActionButton>(R.id.fabDebug)

    // 加载 assets 文件里的全格式 Markdown 内容
    val rawMarkdown = MarkdownLoader.loadFromAssets(this, "full_format_test.md")

    // 使用 MarkdownParser 进一步处理文本
    val parsedMarkdown = MarkdownParser.parse(rawMarkdown)

    // 展示多种配置方式

    // 1. 传统配置方式
    val traditionalConfig = MarkdownConfig(
      enableHtml = true, enableTables = true, enableTaskList = true
    )

    // 2. 使用预设配置
    val blogConfig = MarkdownConfig.blog()
    val chatConfig = MarkdownConfig.chat()
    val editorConfig = MarkdownConfig.editor()

    // 3. 使用 DSL 配置（推荐方式）
    val dslConfig = markdownConfig {
      tables()
      taskLists()
      latex()
      safeMode() // 禁用 HTML 以提高安全性
      imageSize(800, 600)
      async() // 启用异步渲染
      debug() // 启用调试模式
      plugin("syntax-highlight")
    }

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

    // 初始化 MarkdownEngine - 使用 DSL 配置
    val engine = MarkdownEngine.with(this) {
      tables()
      taskLists()
      latex()
      imageSize(800, 600)
      async()
      debug()
    }

    // 先使用 MarkdownEngine 渲染一次内容（第一步目标：暂不渲染，使用调试按钮触发）
//    engine.render(markdownView, parsedMarkdown)

    markdownView.movementMethod = LinkMovementMethod.getInstance()

    // 耗时按钮默认收起
    fabTiming.shrink()
    fabTiming.setOnClickListener {
      if (fabTiming.isExtended) fabTiming.shrink() else fabTiming.extend()
    }

    // 点击调试按钮
    fabDebug.setOnClickListener {
      MarkdownDebugRenderer.renderWithTiming(
        engine.getMarkwon(), markdownView, parsedMarkdown, async = !isSyncMode
      ) { duration ->
        fabTiming.text = if (isSyncMode) "同步耗时 ${duration}ms" else "异步耗时 ${duration}ms"
        fabTiming.extend()
        Log.d("MarkdownDebug", "渲染完成，耗时: ${duration}ms")
      }
      // 切换模式
      isSyncMode = !isSyncMode
    }

    // 添加跳转到 Compose 演示的按钮
    val fabCompose = findViewById<ExtendedFloatingActionButton>(R.id.fab_compose)
    fabCompose.setOnClickListener {
      startActivity(Intent(this, ComposeActivity::class.java))
    }
  }
}
