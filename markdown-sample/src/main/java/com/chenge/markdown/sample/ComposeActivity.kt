package com.chenge.markdown.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.chenge.markdown.common.markdownConfig
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.engine.MarkdownLoader
import com.chenge.markdown.engine.MarkdownParser
import com.chenge.markdown.engine.MarkdownView
import com.chenge.markdown.debug.MarkdownDebugRenderer
import kotlinx.coroutines.delay

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarkdownSampleTheme {
                MarkdownDemoScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownDemoScreen() {
    val context = LocalContext.current
    var isAsyncMode by remember { mutableStateOf(false) }
    var isStreamMode by remember { mutableStateOf(false) }
    var renderTime by remember { mutableStateOf("未渲染") }
    var isRendering by remember { mutableStateOf(false) }
    
    // 加载示例 Markdown 内容
    val markdownContent = remember {
        MarkdownLoader.loadFromAssets(context, "full_format_test.md")
    }
    
    val parsedMarkdown = remember(markdownContent) {
        MarkdownParser.parse(markdownContent)
    }
    
    // 创建 MarkdownEngine
    val engine = remember {
        MarkdownEngine.with(context) {
            tables()
            taskLists()
            latex()
            imageSize(800, 600)
            if (isAsyncMode) async()
            debug()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Markdown SDK 演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 控制面板
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "渲染设置",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 同步/异步切换
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Switch(
                        checked = isAsyncMode,
                        onCheckedChange = { isAsyncMode = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isAsyncMode) "异步渲染" else "同步渲染")
                }
                
                // 流式渲染开关
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Switch(
                        checked = isStreamMode,
                        onCheckedChange = { isStreamMode = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isStreamMode) "流式渲染" else "普通渲染")
                }
                
                // 渲染按钮和耗时显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            // TODO: 实现渲染逻辑
                            isRendering = true
                            renderTime = "渲染中..."
                        },
                        enabled = !isRendering
                    ) {
                        if (isRendering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("开始渲染")
                    }
                    
                    Text(
                        text = "耗时: $renderTime",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Markdown 渲染区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "渲染结果",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                AndroidView(
                    factory = { context ->
                        MarkdownView(context).apply {
                            // 初始化 MarkdownView
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) { markdownView ->
                    // 当渲染参数改变时，重新渲染
                    if (isRendering) {
                        LaunchedEffect(isAsyncMode, isStreamMode) {
                            val startTime = System.currentTimeMillis()
                            
                            try {
                                if (isStreamMode) {
                                    // TODO: 实现流式渲染
                                    delay(100) // 模拟渲染时间
                                } else {
                                    MarkdownDebugRenderer.renderWithTiming(
                                        engine.getMarkwon(),
                                        markdownView,
                                        parsedMarkdown,
                                        async = isAsyncMode
                                    ) { duration ->
                                        renderTime = "${duration}ms"
                                        isRendering = false
                                    }
                                }
                            } catch (e: Exception) {
                                renderTime = "渲染失败: ${e.message}"
                                isRendering = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownSampleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}