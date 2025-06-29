package com.chenge.markdown.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.MarkdownSanitizer
import com.chenge.markdown.core.EmojiReplacer
import com.chenge.markdown.core.MarkdownLoader
import com.chenge.markdown.core.MarkdownEngine
import com.chenge.markdown.core.MarkdownParser
import com.chenge.markdown.debug.MarkdownDebugRenderer
import com.chenge.markdown.plugins.ClickablePlugin
import com.chenge.markdown.plugins.ImageSizePlugin
import com.chenge.markdown.plugins.MarkdownPlugins
import com.chenge.markdown.render.MarkdownView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

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

    // 初始化 MarkdownEngine
    val engine = MarkdownEngine.with(this).config(config)

    // 先使用 MarkdownEngine 渲染一次内容
//    engine.render(markdownView, parsedMarkdown)

    markdownView.movementMethod = android.text.method.LinkMovementMethod.getInstance()

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
  }
}
