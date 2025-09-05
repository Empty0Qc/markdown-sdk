package com.chenge.markdown.compose

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chenge.markdown.compose.ui.screen.MarkdownDemoScreen
import com.chenge.markdown.compose.ui.theme.MarkdownComposeTheme
import com.chenge.markdown.engine.MarkdownEngine
// import com.chenge.markdown.plugins.syntax.SyntaxHighlightPlugin

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
    // 截图对比器（Compose 模块）
    private lateinit var screenshotComparator: ScreenshotComparator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化 Markdown 引擎
        initializeMarkdownEngine()
        // 初始化截图对比器
        screenshotComparator = ScreenshotComparator(this)

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
                                // 每次更新后做一次对比（可按需注释）
                                performScreenshotComparison()
                            },
                            onRenderSpeedChange = { newSpeed ->
                                currentRenderSpeed = newSpeed
                                renderSpeed = newSpeed
                                // 渲染速度调整后进行对比
                                performScreenshotComparison()
                            },
                            onFirstStableFrame = {
                                // 首帧稳定后触发一次截图对比
                                performScreenshotComparison()
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
            R.id.action_update_baseline -> {
                updateBaselineScreenshots()
                true
            }
            R.id.action_show_screenshot_path -> {
                showScreenshotDirectoryInfo()
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

    /**
     * 展示截图目录路径与统计
     */
    private fun showScreenshotDirectoryInfo() {
        val info = screenshotComparator.getDirectoryInfo()
        val path = info["screenshotDir"] as? String ?: ""
        val baseline = info["baselineDir"] as? String ?: ""
        val diff = info["diffDir"] as? String ?: ""
        val currentCount = info["currentCount"]
        val baselineCount = info["baselineCount"]
        val diffCount = info["diffCount"]
        val msg = "current: $currentCount | baseline: $baselineCount | diff: $diffCount\n$path\n$baseline\n$diff"
        showToast(msg)
        android.util.Log.i("ScreenshotTest", "Screenshot dirs =>\n$msg")
    }

    private fun performScreenshotComparison() {
        // 在 Compose 中，我们通过 Window DecorView 截图
        val rootView = window?.decorView?.rootView ?: return
        val screenshotName = generateScreenshotName()
        val bitmap = screenshotComparator.captureView(rootView, screenshotName)
        if (bitmap == null) {
            android.util.Log.w("ScreenshotTest", "Compose: 截图失败，无法进行对比")
            return
        }
        val comparison = screenshotComparator.compareWithBaseline(screenshotName)
        logScreenshotComparisonResult(screenshotName, comparison)
    }

    private fun updateBaselineScreenshots() {
        val rootView = window?.decorView?.rootView ?: return
        val name = generateScreenshotName()
        val bmp = screenshotComparator.captureView(rootView, name)
        if (bmp == null) {
            showToast("截图失败，无法更新基准")
            return
        }
        if (screenshotComparator.updateBaseline(name)) {
            showToast("已更新基准：$name")
        } else {
            showToast("未找到当前截图，无法更新基准")
        }
    }

    private fun generateScreenshotName(): String {
        val theme = if (isDarkMode) "dark" else "light"
        val size = "${fontSize.toInt()}sp"
        val speed = renderSpeed.name.lowercase()
        return "compose_demo_${theme}_${size}_${speed}"
    }

    private fun logScreenshotComparisonResult(
        screenshotName: String,
        result: ScreenshotComparator.ComparisonResult
    ) {
        val tag = "ScreenshotTest"
        if (result.error != null) {
            android.util.Log.e(tag, "对比失败: ${result.error}")
            showToast("对比失败: ${result.error}")
            return
        }
        if (result.isMatch) {
            android.util.Log.i(tag, "对比通过: $screenshotName 差异 ${"%.2f".format(result.diffPercentage)}%")
            showToast("对比通过：${"%.2f".format(result.diffPercentage)}%")
        } else {
            android.util.Log.w(tag, "对比不通过: $screenshotName 差异 ${"%.2f".format(result.diffPercentage)}% diff: ${result.diffImagePath}")
            showToast("对比不通过：${"%.2f".format(result.diffPercentage)}%")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownDemoPreview() {
    MarkdownComposeTheme {
        MarkdownDemoScreen()
    }
}
