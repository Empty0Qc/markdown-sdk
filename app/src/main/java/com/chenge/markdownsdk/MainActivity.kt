package com.chenge.markdownsdk


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

import androidx.core.view.isVisible

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
    private lateinit var screenshotComparator: ScreenshotComparator // 截图对比器

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

        // 初始化截图对比器
        screenshotComparator = ScreenshotComparator(this)
    }

    private fun setupUI() {
        // 初始化渐进式渲染器
        progressiveRenderer = ProgressiveRenderer()
        progressiveRenderer.setRenderMode(renderMode)
        progressiveRenderer.setRenderSpeed(renderSpeed)
        progressiveRenderer.setProgressCallback { _, message ->
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

        // 设置跳转到底部按钮
        binding.jumpToBottomFab.setOnClickListener {
            jumpToBottom()
        }

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

                // 加载综合性测试 Markdown 内容
                val rawMarkdown = MarkdownLoader.loadFromAssets(this@MainActivity, "comprehensive_test.md")

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
                binding.contentTextView.getTextView().text = getString(R.string.error_loading_content) + ": ${e.message}"
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

                    // 记录设备、窗口、视图维度的调试信息
                    logRenderDebugInfo()

                    // 获取渲染后的Spanned内容
                    val renderedContent = binding.contentTextView.getTextView().text as? android.text.Spanned
                        ?: android.text.SpannableString(binding.contentTextView.getTextView().text)

                    // 设置自动滚动管理器
                    progressiveRenderer.setAutoScrollManager(
                        binding.swipeRefreshLayout.getChildAt(0) as androidx.core.widget.NestedScrollView,
                        binding.contentTextView.getTextView(),
                        this@MainActivity
                    )

                    // 设置用户滚动状态监听器
                    progressiveRenderer.getAutoScrollManager()?.setOnUserScrollStateChangedListener { isUserScrolling ->
                        runOnUiThread {
                            binding.jumpToBottomFab.visibility = if (isUserScrolling) {
                                android.view.View.VISIBLE
                            } else {
                                android.view.View.GONE
                            }
                        }
                    }

                    // 启用自动滚动（在所有模式下都启用）
                    progressiveRenderer.setAutoScrollEnabled(true)

                    // 强制恢复自动滚动
                    progressiveRenderer.getAutoScrollManager()?.forceResumeAutoScroll()

                    // 使用统一的渐进式渲染器
                    progressiveRenderer.startRender(
                        binding.contentTextView.getTextView(),
                        renderedContent,
                        this@MainActivity
                    ) {
                        // 渲染完成回调
                        binding.timeTextView.text = getString(R.string.render_time, duration)

                        // 渲染完成后再次记录视觉验证信息
                        logVisualVerificationInfo()

                        // 执行自动化截图对比测试
                        performScreenshotComparison()
                    }
                }
            }
        }

        if (isSyncMode) {
            // 同步渲染
            markdownEngine.getMarkwon().setMarkdown(binding.contentTextView.getTextView(), content)
            renderComplete()
        } else {
            // 异步渲染
            lifecycleScope.launch {
                markdownEngine.getMarkwon().setMarkdown(binding.contentTextView.getTextView(), content)
                renderComplete()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_toggle_theme -> {
                toggleTheme(); return true
            }
            R.id.action_typewriter_slow -> {
                setRenderSpeed(ProgressiveRenderer.RenderSpeed.SLOW); return true
            }
            R.id.action_typewriter_normal -> {
                setRenderSpeed(ProgressiveRenderer.RenderSpeed.NORMAL); return true
            }
            R.id.action_typewriter_fast -> {
                setRenderSpeed(ProgressiveRenderer.RenderSpeed.FAST); return true
            }
            R.id.action_increase_font -> {
                adjustFontSize(1.1f); return true
            }
            R.id.action_decrease_font -> {
                adjustFontSize(0.9f); return true
            }
            R.id.action_reset_font -> {
                resetFontSize(); return true
            }
            R.id.action_high_contrast -> {
                toggleHighContrast(); return true
            }
            R.id.action_about -> {
                showAboutDialog(); return true
            }
            // Remove the action_update_baseline case completely
        }
        return super.onOptionsItemSelected(item)
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

    private fun showAboutDialog() {
        val aboutDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        aboutDialog.show()
    }

    private fun setRenderSpeed(speed: ProgressiveRenderer.RenderSpeed) {
        renderSpeed = speed
        progressiveRenderer.setRenderSpeed(speed)
        Log.d("MainActivity", "Render speed changed to: $speed")

        // 重新渲染当前内容以应用新的渲染速度
        binding.contentTextView.post {
            loadAndRenderContent()
        }
    }

    private fun cycleRenderMode() {
        renderMode = when (renderMode) {
            ProgressiveRenderer.RenderMode.INSTANT -> ProgressiveRenderer.RenderMode.PROGRESSIVE
            ProgressiveRenderer.RenderMode.PROGRESSIVE -> ProgressiveRenderer.RenderMode.CHUNKED
            ProgressiveRenderer.RenderMode.CHUNKED -> ProgressiveRenderer.RenderMode.INSTANT
        }
        setRenderMode(renderMode)
    }

    private fun cycleRenderSpeed() {
        renderSpeed = when (renderSpeed) {
            ProgressiveRenderer.RenderSpeed.SLOW -> ProgressiveRenderer.RenderSpeed.NORMAL
            ProgressiveRenderer.RenderSpeed.NORMAL -> ProgressiveRenderer.RenderSpeed.FAST
            ProgressiveRenderer.RenderSpeed.FAST -> ProgressiveRenderer.RenderSpeed.INSTANT
            ProgressiveRenderer.RenderSpeed.INSTANT -> ProgressiveRenderer.RenderSpeed.SLOW
        }
        progressiveRenderer.setRenderSpeed(renderSpeed)
        Log.d("MainActivity", "Render speed changed to: $renderSpeed")

        // 重新渲染当前内容以应用新的渲染速度
        binding.contentTextView.post {
            loadAndRenderContent()
        }
    }

    private fun jumpToBottom() {
        // 使用AutoScrollManager跳转到底部
        progressiveRenderer.getAutoScrollManager()?.jumpToBottom()

        // 强制恢复自动滚动
        progressiveRenderer.getAutoScrollManager()?.forceResumeAutoScroll()

        // 确保自动滚动启用
        progressiveRenderer.setAutoScrollEnabled(true)

        // 隐藏跳转按钮
        binding.jumpToBottomFab.visibility = android.view.View.GONE
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

    /**
     * 记录设备、窗口、视图维度的调试信息
     */
    private fun logRenderDebugInfo() {
        try {
            val resources = resources
            val displayMetrics = resources.displayMetrics
            val configuration = resources.configuration

            // 设备信息
            val deviceInfo = "device: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}px, " +
                    "densityDpi=${displayMetrics.densityDpi}, density=${displayMetrics.density}, " +
                    "orientation=${if (configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"}"

            // 窗口信息
            val windowInsets = ViewCompat.getRootWindowInsets(binding.root)
            val systemBarsInsets = windowInsets?.getInsets(WindowInsetsCompat.Type.systemBars())
            val windowInfo = "window: systemBars(${systemBarsInsets?.left},${systemBarsInsets?.top},${systemBarsInsets?.right},${systemBarsInsets?.bottom})"

            // 获取窗口可见区域
            val windowVisibleRect = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(windowVisibleRect)
            val windowVisibleInfo = "windowVisible: ${windowVisibleRect.width()}x${windowVisibleRect.height()} at (${windowVisibleRect.left},${windowVisibleRect.top})"

            // ContentTextView 信息
            val contentView = binding.contentTextView
            val textView = contentView.getTextView()

            // 测量信息
            val contentMeasured = "contentView: measured=${contentView.measuredWidth}x${contentView.measuredHeight}"
            val textMeasured = "textView: measured=${textView.measuredWidth}x${textView.measuredHeight}"

            // 位置信息
            val contentLocation = IntArray(2)
            contentView.getLocationOnScreen(contentLocation)
            val textLocation = IntArray(2)
            textView.getLocationOnScreen(textLocation)
            val contentPosition = "contentPos: screen(${contentLocation[0]},${contentLocation[1]})"
            val textPosition = "textPos: screen(${textLocation[0]},${textLocation[1]})"

            // 可见性信息
            val contentVisible = "contentVisible: isShown=${contentView.isShown}, alpha=${contentView.alpha}, visibility=${contentView.visibility}"
            val textVisible = "textVisible: isShown=${textView.isShown}, alpha=${textView.alpha}, visibility=${textView.visibility}"

            // 滚动信息
            val scrollView = binding.swipeRefreshLayout.getChildAt(0) as androidx.core.widget.NestedScrollView
            val scrollInfo = "scroll: canScroll=${scrollView.canScrollVertically(1) || scrollView.canScrollVertically(-1)}, " +
                    "scrollY=${scrollView.scrollY}, scrollRange=${scrollView.getChildAt(0)?.height?.minus(scrollView.height) ?: 0}"

            Log.d("RenderDebug", deviceInfo)
            Log.d("RenderDebug", windowInfo)
            Log.d("RenderDebug", windowVisibleInfo)
            Log.d("RenderDebug", contentMeasured)
            Log.d("RenderDebug", textMeasured)
            Log.d("RenderDebug", contentPosition)
            Log.d("RenderDebug", textPosition)
            Log.d("RenderDebug", contentVisible)
            Log.d("RenderDebug", textVisible)
            Log.d("RenderDebug", scrollInfo)

        } catch (e: Exception) {
            Log.e("RenderDebug", "记录调试信息失败", e)
        }
    }

    /**
     * 记录视觉验证信息，包括遮挡检测和可见面积计算
     */
    private fun logVisualVerificationInfo() {
        try {
            val contentView = binding.contentTextView
            val progressBar = binding.progressBar
            val textView = contentView.getTextView()

            // 获取全局可见矩形
            val contentGlobalRect = android.graphics.Rect()
            val progressGlobalRect = android.graphics.Rect()
            val textGlobalRect = android.graphics.Rect()

            contentView.getGlobalVisibleRect(contentGlobalRect)
            progressBar.getGlobalVisibleRect(progressGlobalRect)
            textView.getGlobalVisibleRect(textGlobalRect)

            // 计算可见面积
            val contentTotalArea = contentView.measuredWidth.toLong() * contentView.measuredHeight
            val contentVisibleArea = contentGlobalRect.width().toLong() * contentGlobalRect.height()
            val contentVisibilityRatio = if (contentTotalArea > 0) {
                (contentVisibleArea * 100.0 / contentTotalArea)
            } else 0.0

            val textTotalArea = textView.measuredWidth.toLong() * textView.measuredHeight
            val textVisibleArea = textGlobalRect.width().toLong() * textGlobalRect.height()
            val textVisibilityRatio = if (textTotalArea > 0) {
                (textVisibleArea * 100.0 / textTotalArea)
            } else 0.0

            // 检测遮挡 - ProgressBar 是否与 ContentView 相交
            val progressVisible = progressBar.isVisible
            val isOccluded = progressVisible && android.graphics.Rect.intersects(contentGlobalRect, progressGlobalRect)

            // 检测可见性阈值
            val contentVisibilityThreshold = 50.0 // 50%
            val textVisibilityThreshold = 50.0
            val contentVisibilityInsufficient = contentVisibilityRatio < contentVisibilityThreshold
            val textVisibilityInsufficient = textVisibilityRatio < textVisibilityThreshold

            // 输出验证信息
            val contentAreaInfo = "contentArea: total=${contentTotalArea}, visible=${contentVisibleArea}, ratio=${"%.1f".format(contentVisibilityRatio)}%"
            val textAreaInfo = "textArea: total=${textTotalArea}, visible=${textVisibleArea}, ratio=${"%.1f".format(textVisibilityRatio)}%"
            val occlusionInfo = "occlusion: progressVisible=${progressVisible}, intersect=${isOccluded}"
            val visibilityWarning = when {
                contentVisibilityInsufficient && textVisibilityInsufficient ->
                    "visibility: WARNING - 内容和文本可见性均不足"
                contentVisibilityInsufficient ->
                    "visibility: WARNING - 内容可见性不足"
                textVisibilityInsufficient ->
                    "visibility: WARNING - 文本可见性不足"
                isOccluded ->
                    "visibility: WARNING - 检测到视图遮挡"
                else ->
                    "visibility: OK - 可见性正常"
            }

            Log.d("RenderDebug", contentAreaInfo)
            Log.d("RenderDebug", textAreaInfo)
            Log.d("RenderDebug", occlusionInfo)
            Log.d("RenderDebug", visibilityWarning)

            // 边界检测
            val windowVisibleRect = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(windowVisibleRect)
            val contentOutOfBounds = !windowVisibleRect.contains(contentGlobalRect)
            val textOutOfBounds = !windowVisibleRect.contains(textGlobalRect)

            if (contentOutOfBounds || textOutOfBounds) {
                Log.w("RenderDebug", "bounds: WARNING - 内容超出窗口可见区域")
            } else {
                Log.d("RenderDebug", "bounds: OK - 内容在窗口可见区域内")
            }

        } catch (e: Exception) {
            Log.e("RenderDebug", "记录视觉验证信息失败", e)
        }
    }

    /**
     * 执行自动化截图对比测试
     */
    private fun performScreenshotComparison() {
        // 等待一小段时间确保渲染完全完成
        binding.contentTextView.postDelayed({
            try {
                // 生成截图文件名（包含当前设置）
                val screenshotName = generateScreenshotName()

                // 捕获当前视图截图
                val screenshot = screenshotComparator.captureView(
                    binding.contentTextView,
                    screenshotName
                )

                if (screenshot != null) {
                    // 与基准截图对比
                    val comparisonResult = screenshotComparator.compareWithBaseline(screenshotName)

                    // 记录对比结果
                    logScreenshotComparisonResult(screenshotName, comparisonResult)

                    // 如果差异较大，可以选择更新基准（在开发阶段）
                    if (!comparisonResult.isMatch && comparisonResult.error == null) {
                        Log.w("ScreenshotTest", "Visual regression detected in $screenshotName: " +
                                "${String.format("%.2f", comparisonResult.diffPercentage)}% difference")

                        // 在开发环境中，可以选择自动更新基准
                        if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                            Log.i("ScreenshotTest", "Development mode: consider updating baseline if changes are expected")
                        }
                    }
                } else {
                    Log.e("ScreenshotTest", "Failed to capture screenshot for comparison")
                }

                // 定期清理旧的截图文件
                screenshotComparator.cleanupOldScreenshots()

            } catch (e: Exception) {
                Log.e("ScreenshotTest", "Screenshot comparison failed", e)
            }
        }, 500) // 等待500ms确保渲染完成
    }

    /**
     * 生成截图文件名，包含当前配置信息
     */
    private fun generateScreenshotName(): String {
        val themeMode = if (isDarkMode) "dark" else "light"
        val contrastMode = if (isHighContrastMode) "high_contrast" else "normal"
        val fontSize = currentFontSize.toInt()
        val renderModeStr = renderMode.name.lowercase()
        val renderSpeedStr = renderSpeed.name.lowercase()

        return "comprehensive_test_${themeMode}_${contrastMode}_${fontSize}sp_${renderModeStr}_${renderSpeedStr}"
    }

    /**
     * 记录截图对比结果
     */
    private fun logScreenshotComparisonResult(screenshotName: String, result: ScreenshotComparator.ComparisonResult) {
        val logLevel = if (result.isMatch) Log.INFO else Log.WARN
        val status = if (result.isMatch) "PASS" else "FAIL"

        Log.println(logLevel, "ScreenshotTest",
            "[$status] Screenshot comparison for '$screenshotName': " +
            "match=${result.isMatch}, " +
            "diff=${String.format("%.2f", result.diffPercentage)}%, " +
            "pixels=${result.diffPixelCount}/${result.totalPixelCount}"
        )

        if (result.diffImagePath != null) {
            Log.i("ScreenshotTest", "Diff image saved: ${result.diffImagePath}")
        }

        if (result.error != null) {
            Log.e("ScreenshotTest", "Comparison error: ${result.error}")
        }

        // 记录目录信息（仅在首次运行时）
        if (screenshotName.contains("normal_16sp_progressive_normal")) {
            val dirInfo = screenshotComparator.getDirectoryInfo()
            Log.i("ScreenshotTest", "Directory info: $dirInfo")
        }
    }


}
