package com.chenge.markdown.compose.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.chenge.markdown.compose.R
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.engine.config.MarkdownConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var markdownText by remember { mutableStateOf(getSampleMarkdown()) }
    var renderedHtml by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val markdownEngine = remember { MarkdownEngine.getInstance() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 基本渲染示例
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.markdown_example_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = ""
                                    renderedHtml = markdownEngine.render(markdownText)
                                } catch (e: Exception) {
                                    errorMessage = "渲染失败: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.render_button))
                    }
                    
                    Button(
                        onClick = {
                            renderedHtml = ""
                            errorMessage = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.clear_button))
                    }
                }
            }
        }
        
        // 异步渲染示例
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.async_render_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = ""
                                val result = withContext(Dispatchers.IO) {
                                    markdownEngine.renderAsync(markdownText)
                                }
                                renderedHtml = result
                            } catch (e: Exception) {
                                errorMessage = "异步渲染失败: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.async_render_button))
                }
            }
        }
        
        // 自定义配置示例
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.custom_config_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = ""
                                val config = MarkdownConfig.Builder()
                                    .enableTables(true)
                                    .enableCodeHighlight(true)
                                    .enableMath(true)
                                    .build()
                                renderedHtml = markdownEngine.render(markdownText, config)
                            } catch (e: Exception) {
                                errorMessage = "自定义配置渲染失败: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("使用自定义配置渲染")
                }
            }
        }
        
        // 状态显示
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        if (errorMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 渲染结果显示
        if (renderedHtml.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "渲染结果:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 使用 AndroidView 显示 HTML 内容
                    AndroidView(
                        factory = { context ->
                            android.webkit.WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                null,
                                renderedHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
}

private fun getSampleMarkdown(): String {
    return """
# Markdown 示例

这是一个 **Compose** 版本的 Markdown 渲染示例。

## 功能特性

- 支持 *斜体* 和 **粗体**
- 支持 `代码块`
- 支持链接: [GitHub](https://github.com)

### 代码示例

```kotlin
fun main() {
    println("Hello, Markdown!")
}
```

### 表格示例

| 功能 | 状态 | 描述 |
|------|------|------|
| 基本渲染 | ✅ | 支持基本 Markdown 语法 |
| 异步渲染 | ✅ | 支持后台异步处理 |
| 自定义配置 | ✅ | 支持个性化配置 |

> 这是一个引用块示例

### 数学公式

行内公式: $E = mc^2$

块级公式:
$$\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}$$
    """.trimIndent()
}