package com.chenge.markdownsdk

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.chenge.markdown.engine.ImageSizePlugin
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.engine.MarkdownLoader
import com.chenge.markdown.engine.MarkdownParser
import com.chenge.markdown.engine.MarkdownPlugins
import com.chenge.markdownsdk.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * 主Activity - 现代化Markdown渲染器演示
 *
 * 功能特性：
 * - Material Design 3 主题
 * - 深色/浅色模式切换
 * - 完整的Markdown语法支持
 * - 性能测试和调试功能
 * - 多种渲染模式（同步/异步）

 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var markdownEngine: MarkdownEngine
    private var isSyncMode = true // 初始为同步模式
    private var isDarkMode = false
    private var isHighContrastMode = false
    private var currentFontSize = 16f // 默认字体大小
    private lateinit var progressiveRenderer: ProgressiveRenderer // 统一的渐进式渲染器
    private var renderMode = ProgressiveRenderer.RenderMode.PROGRESSIVE // 渲染模式
    private var renderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL // 渲染速度

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        // 设置系统栏
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeMarkdownEngine()

        setupUI()
        loadAndRenderContent()
    }

    private fun initializeMarkdownEngine() {
        // 注册插件
        MarkdownPlugins.register(ImageSizePlugin(this))
        // 移除 ClickablePlugin 以消除交互式覆盖层
        // MarkdownPlugins.register(
        //   ClickablePlugin(
        //     onLinkClick = { url ->
        //       try {
        //         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        //         startActivity(intent)
        //       } catch (e: Exception) {
        //         // 无法打开链接
        //       }
        //     },
        //     onCodeClick = { code ->
        //       Log.d("CodeClick", "点击代码: $code")
        //       // 代码点击事件
        //     }
        //   )
        // )

        // 使用 DSL 配置初始化引擎
        markdownEngine = MarkdownEngine.with(this) {
            tables()
            taskLists()
            latex()
            imageSize(800, 600)
            async()
            debug()
            plugin("syntax-highlight")
        }
    }

    private fun setupUI() {
        // 初始化渐进式渲染器
        progressiveRenderer = ProgressiveRenderer()
        progressiveRenderer.setRenderMode(renderMode)
        progressiveRenderer.setRenderSpeed(renderSpeed)
        progressiveRenderer.setProgressCallback { progress, message ->
            runOnUiThread {
                binding.timeTextView.text = message
            }
        }

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadAndRenderContent()
        }

        // 设置浮动操作按钮
        binding.fab.setOnClickListener {
            toggleTheme()
        }

        // 移除LinkMovementMethod以消除链接点击的交互式覆盖层
        // binding.contentTextView.movementMethod = LinkMovementMethod.getInstance()

        // 设置底部按钮点击事件
        binding.debugRenderButton.setOnClickListener {
            toggleRenderMode()
        }

        // 渐进式渲染模式切换按钮
        binding.streamingModeButton.setOnClickListener {
            cycleRenderMode()
        }

        // 渲染速度切换按钮
        binding.typewriterButton.setOnClickListener {
            cycleRenderSpeed()
        }
    }

    private fun loadAndRenderContent() {
        lifecycleScope.launch {
            try {
                // 显示加载状态
                binding.swipeRefreshLayout.isRefreshing = true
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.contentTextView.visibility = android.view.View.GONE

                // 加载 assets 文件里的全格式 Markdown 内容
                val rawMarkdown = MarkdownLoader.loadFromAssets(this@MainActivity, "full_format_test.md")

                // 使用 MarkdownParser 进一步处理文本
                val parsedMarkdown = MarkdownParser.parse(rawMarkdown)

                // 渲染内容
                renderMarkdownContent(parsedMarkdown)
            } catch (e: Exception) {
                Log.e("MainActivity", "加载内容失败", e)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_loading_content) + ": ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                // 隐藏加载状态，显示错误
                binding.progressBar.visibility = android.view.View.GONE
                binding.contentTextView.visibility = android.view.View.VISIBLE
                binding.contentTextView.text = getString(R.string.error_loading_content) + ": ${e.message}"
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun renderMarkdownContent(content: String) {
        // 检查 Activity 是否已销毁
        if (isDestroyed || isFinishing) {
            Log.w("MainActivity", "Activity 已销毁，跳过渲染")
            return
        }

        // 直接使用markdownEngine进行渲染
        val startTime = System.currentTimeMillis()

        val renderComplete = {
            val duration = System.currentTimeMillis() - startTime
            val modeText = if (isSyncMode) "同步" else "异步"
            Log.d("MarkdownRender", "${modeText}渲染完成，耗时: ${duration}ms")

            // 再次检查 Activity 状态
            if (!isDestroyed && !isFinishing) {
                runOnUiThread {
                    // 隐藏加载状态，显示内容
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.contentTextView.visibility = android.view.View.VISIBLE

                    // 获取渲染后的Spanned内容
                    val renderedContent = binding.contentTextView.text as? android.text.Spanned
                        ?: android.text.SpannableString(binding.contentTextView.text)

                    // 设置自动滚动管理器
                    progressiveRenderer.setAutoScrollManager(
                        binding.swipeRefreshLayout.getChildAt(0) as androidx.core.widget.NestedScrollView,
                        binding.contentTextView,
                        this@MainActivity
                    )

                    // 启用自动滚动（仅在渐进式模式下）
                    progressiveRenderer.setAutoScrollEnabled(
                        renderMode == ProgressiveRenderer.RenderMode.PROGRESSIVE ||
                            renderMode == ProgressiveRenderer.RenderMode.CHUNKED
                    )

                    // 使用统一的渐进式渲染器
                    progressiveRenderer.startRender(
                        binding.contentTextView,
                        renderedContent,
                        this@MainActivity
                    ) {
                        // 渲染完成回调
                        binding.timeTextView.text = getString(R.string.render_time, duration)
                    }
                }
            }
        }

        if (isSyncMode) {
            // 同步渲染
            markdownEngine.getMarkwon().setMarkdown(binding.contentTextView, content)
            renderComplete()
        } else {
            // 异步渲染
            lifecycleScope.launch {
                markdownEngine.getMarkwon().setMarkdown(binding.contentTextView, content)
                renderComplete()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                toggleTheme()
                true
            }

            R.id.action_high_contrast -> {
                toggleHighContrast()
                true
            }

            R.id.action_increase_font -> {
                adjustFontSize(1.2f)
                true
            }

            R.id.action_decrease_font -> {
                adjustFontSize(0.8f)
                true
            }

            R.id.action_reset_font -> {
                resetFontSize()
                true
            }

            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        // 主题切换完成
    }

    private fun toggleRenderMode() {
        isSyncMode = !isSyncMode
        val modeText = if (isSyncMode) "同步" else "异步"
        // 已切换到${modeText}渲染模式

        // 重新渲染内容以测试新模式
        loadAndRenderContent()
    }

    private fun adjustFontSize(factor: Float) {
        val newSize = currentFontSize * factor

        if (newSize >= 10f && newSize <= 36f) {
            currentFontSize = newSize
            binding.contentTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, currentFontSize)
            // 字体大小已调整
        } else {
            // 字体大小已达到限制
        }
    }

    private fun resetFontSize() {
        currentFontSize = 16f
        binding.contentTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, currentFontSize)
        // 字体大小已重置
    }

    private fun toggleHighContrast() {
        isHighContrastMode = !isHighContrastMode

        if (isHighContrastMode) {
            // 启用高对比度模式
            binding.contentTextView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            binding.contentTextView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.black
                )
            )
            binding.root.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
        } else {
            // 恢复正常模式
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            binding.contentTextView.setTextColor(ContextCompat.getColor(this, typedValue.resourceId))

            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            binding.contentTextView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    typedValue.resourceId
                )
            )
            binding.root.setBackgroundColor(ContextCompat.getColor(this, typedValue.resourceId))
        }

        // 高对比度模式已切换
    }

    private fun setRenderMode(mode: ProgressiveRenderer.RenderMode) {
        renderMode = mode
        progressiveRenderer.setRenderMode(mode)
        val modeText = when (mode) {
            ProgressiveRenderer.RenderMode.INSTANT -> "立即显示"
            ProgressiveRenderer.RenderMode.PROGRESSIVE -> "逐字符渐进"
            ProgressiveRenderer.RenderMode.CHUNKED -> "分块显示"
        }
        Toast.makeText(this, "渲染模式已设置为: $modeText", Toast.LENGTH_SHORT).show()

        // 重新渲染内容以应用新模式
        loadAndRenderContent()
    }

    private fun setRenderSpeed(speed: ProgressiveRenderer.RenderSpeed) {
        renderSpeed = speed
        progressiveRenderer.setRenderSpeed(speed)
        val speedText = when (speed) {
            ProgressiveRenderer.RenderSpeed.SLOW -> "慢速"
            ProgressiveRenderer.RenderSpeed.NORMAL -> "正常"
            ProgressiveRenderer.RenderSpeed.FAST -> "快速"
            ProgressiveRenderer.RenderSpeed.INSTANT -> "瞬时"
        }
        // 渲染速度已设置
    }

    private fun cycleRenderMode() {
        val nextMode = when (renderMode) {
            ProgressiveRenderer.RenderMode.INSTANT -> ProgressiveRenderer.RenderMode.PROGRESSIVE
            ProgressiveRenderer.RenderMode.PROGRESSIVE -> ProgressiveRenderer.RenderMode.CHUNKED
            ProgressiveRenderer.RenderMode.CHUNKED -> ProgressiveRenderer.RenderMode.INSTANT
        }
        setRenderMode(nextMode)

        // 更新按钮文本
        val buttonText = when (nextMode) {
            ProgressiveRenderer.RenderMode.INSTANT -> "立即显示"
            ProgressiveRenderer.RenderMode.PROGRESSIVE -> "逐字符渐进"
            ProgressiveRenderer.RenderMode.CHUNKED -> "分块显示"
        }
        binding.streamingModeButton.text = buttonText
    }

    private fun cycleRenderSpeed() {
        val nextSpeed = when (renderSpeed) {
            ProgressiveRenderer.RenderSpeed.SLOW -> ProgressiveRenderer.RenderSpeed.NORMAL
            ProgressiveRenderer.RenderSpeed.NORMAL -> ProgressiveRenderer.RenderSpeed.FAST
            ProgressiveRenderer.RenderSpeed.FAST -> ProgressiveRenderer.RenderSpeed.INSTANT
            ProgressiveRenderer.RenderSpeed.INSTANT -> ProgressiveRenderer.RenderSpeed.SLOW
        }
        setRenderSpeed(nextSpeed)

        // 更新按钮文本
        val buttonText = when (nextSpeed) {
            ProgressiveRenderer.RenderSpeed.SLOW -> "慢速"
            ProgressiveRenderer.RenderSpeed.NORMAL -> "正常速度"
            ProgressiveRenderer.RenderSpeed.FAST -> "快速"
            ProgressiveRenderer.RenderSpeed.INSTANT -> "瞬时"
        }
        binding.typewriterButton.text = buttonText
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        // 停止渐进式渲染
        if (::progressiveRenderer.isInitialized) {
            progressiveRenderer.stopRender()
        }

        // 清理资源
        try {
            // 这里可以添加资源清理逻辑
            Log.d("MainActivity", "资源清理完成")
        } catch (e: Exception) {
            Log.e("MainActivity", "资源清理失败", e)
        }
    }
}
