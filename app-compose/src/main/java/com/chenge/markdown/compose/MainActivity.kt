package com.chenge.markdown.compose

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.chenge.markdown.compose.ui.screen.MarkdownDemoScreen
import com.chenge.markdown.compose.ui.theme.MarkdownComposeTheme
import com.chenge.markdown.engine.MarkdownEngine
// import com.chenge.markdown.plugins.syntax.SyntaxHighlightPlugin
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Compose 版本的 Markdown 渲染示例应用
 *
 * 这个 Activity 展示了如何在 Jetpack Compose 中集成和使用 Markdown SDK
 * 包含以下功能演示：
 * - 基本 Markdown 渲染
 * - 异步渲染
 * - 自定义配置
 * - 性能测试
 * - 主题切换
 * - 渐进式渲染
 * - 菜单功能
 */
class MainActivity : AppCompatActivity() {

  private lateinit var markdownEngine: MarkdownEngine
  private var isDarkMode = false
  private var fontSize = 16f
  private var renderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 初始化 Markdown 引擎
    initializeMarkdownEngine()

    setContent {
      var darkTheme by remember { mutableStateOf(isDarkMode) }
      var currentFontSize by remember { mutableStateOf(fontSize) }
      var currentRenderSpeed by remember { mutableStateOf(renderSpeed) }

      MarkdownComposeTheme(darkTheme = darkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Scaffold(
            modifier = Modifier.fillMaxSize()
          ) { innerPadding ->
            MarkdownDemoScreen(
              modifier = Modifier.padding(innerPadding),
              markdownEngine = markdownEngine,
              fontSize = currentFontSize,
              renderSpeed = currentRenderSpeed,
              onThemeChange = {
                darkTheme = !darkTheme
                isDarkMode = darkTheme
                updateSystemTheme()
              },
              onFontSizeChange = { newSize ->
                currentFontSize = newSize
                fontSize = newSize
              },
              onRenderSpeedChange = { newSpeed ->
                currentRenderSpeed = newSpeed
                renderSpeed = newSpeed
              }
            )
          }
        }
      }
    }
  }

  /**
   * 初始化 Markdown 引擎
   */
  private fun initializeMarkdownEngine() {
    markdownEngine = MarkdownEngine.with(this)
  }

  /**
   * 更新系统主题
   */
  private fun updateSystemTheme() {
    val mode = if (isDarkMode) {
      AppCompatDelegate.MODE_NIGHT_YES
    } else {
      AppCompatDelegate.MODE_NIGHT_NO
    }
    AppCompatDelegate.setDefaultNightMode(mode)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_toggle_theme -> {
        isDarkMode = !isDarkMode
        updateSystemTheme()
        recreate() // 重新创建 Activity 以应用主题
        true
      }

      R.id.action_font_size_increase -> {
        fontSize = (fontSize + 2f).coerceAtMost(24f)
        recreate()
        true
      }

      R.id.action_font_size_decrease -> {
        fontSize = (fontSize - 2f).coerceAtLeast(12f)
        recreate()
        true
      }

      R.id.action_font_size_reset -> {
        fontSize = 16f
        recreate()
        true
      }

      R.id.action_speed_slow -> {
        renderSpeed = ProgressiveRenderer.RenderSpeed.SLOW
        showToast("渲染速度：慢速")
        true
      }

      R.id.action_speed_normal -> {
        renderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL
        showToast("渲染速度：正常")
        true
      }

      R.id.action_speed_fast -> {
        renderSpeed = ProgressiveRenderer.RenderSpeed.FAST
        showToast("渲染速度：快速")
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }

  /**
   * 显示 Toast 消息
   */
  private fun showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }
}

@Preview(showBackground = true)
@Composable
fun MarkdownDemoPreview() {
  MarkdownComposeTheme {
    MarkdownDemoScreen()
  }
}