package com.chenge.markdown.compose.ui.screen

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
// import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chenge.markdown.compose.R
import com.chenge.markdown.compose.ProgressiveRenderer
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.render.MarkdownView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownDemoScreen(
    modifier: Modifier = Modifier,
    markdownEngine: MarkdownEngine? = null,
    fontSize: Float = 16f,
    renderSpeed: ProgressiveRenderer.RenderSpeed = ProgressiveRenderer.RenderSpeed.NORMAL,
    onThemeChange: () -> Unit = {},
    onFontSizeChange: (Float) -> Unit = {},
    onRenderSpeedChange: (ProgressiveRenderer.RenderSpeed) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // val lifecycleOwner = LocalLifecycleOwner.current
    
    var markdownText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val progressiveRenderer = remember { ProgressiveRenderer() }
    
    LaunchedEffect(Unit) {
        loadMarkdownContent(context) { content ->
            markdownText = content
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部工具栏
        TopAppBar(
            title = { Text("Markdown Demo") },
            actions = {
                IconButton(onClick = onThemeChange) {
                    Icon(Icons.Default.Palette, contentDescription = "切换主题")
                }
            }
        )
        
        // 控制面板
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "渲染控制",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 字体大小控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("字体大小: ${fontSize.toInt()}sp")
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 12f..24f,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 渲染速度控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("渲染速度: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    ProgressiveRenderer.RenderSpeed.values().forEach { speed ->
                        FilterChip(
                            onClick = { onRenderSpeedChange(speed) },
                            label = { Text(speed.name) },
                            selected = renderSpeed == speed,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 操作按钮
                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    renderMarkdown(
                                        markdownEngine = markdownEngine,
                                        content = markdownText,
                                        isAsync = true,
                                        onStart = { isLoading = true },
                                        onComplete = { isLoading = false },
                                        onError = { error -> 
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("异步渲染")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    renderMarkdown(
                                        markdownEngine = markdownEngine,
                                        content = markdownText,
                                        isAsync = false,
                                        onStart = { isLoading = true },
                                        onComplete = { isLoading = false },
                                        onError = { error -> 
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("同步渲染")
                    }
                }
                
                // 错误信息显示
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "错误: $error",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Markdown 渲染区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { context ->
                    MarkdownView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // 初始化设置将在 update 中处理
                        textSize = fontSize
                        setMarkdown(markdownText)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                update = { markdownView ->
                    markdownView.textSize = fontSize
                    markdownView.setMarkdown(markdownText)
                    // progressiveRenderer.setMarkdownView(markdownView)
                }
            )
        }
    }
}

private suspend fun renderMarkdown(
    markdownEngine: MarkdownEngine?,
    content: String,
    isAsync: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        onStart()
        
        if (markdownEngine == null) {
            onError("MarkdownEngine 未初始化")
            return
        }
        
        // 简化渲染逻辑，直接使用 MarkdownView
        withContext(Dispatchers.Main) {
            // 渲染逻辑已在 AndroidView 的 update 中处理
        }
        
        onComplete()
    } catch (e: Exception) {
        onError(e.message ?: "未知错误")
    }
}

private suspend fun loadMarkdownContent(
    context: Context,
    onLoaded: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("full_format_test.md")
            val content = inputStream.bufferedReader().use { it.readText() }
            withContext(Dispatchers.Main) {
                onLoaded(content)
            }
        } catch (e: IOException) {
            // 如果文件不存在，使用示例内容
            withContext(Dispatchers.Main) {
                onLoaded(getSampleMarkdown())
            }
        }
    }
}

private fun getSampleMarkdown(): String {
    return """
        # Markdown 渲染演示
        
        这是一个 **Markdown** 渲染演示页面。
        
        ## 功能特性
        
        - [x] 支持基本 Markdown 语法
        - [x] 支持表格渲染
        - [x] 支持任务列表
        - [x] 支持代码高亮
        - [ ] 性能优化
        
        > 这是一个引用块，用于展示重要信息。
        
        更多内容请查看完整的测试文件。
    """.trimIndent()
}