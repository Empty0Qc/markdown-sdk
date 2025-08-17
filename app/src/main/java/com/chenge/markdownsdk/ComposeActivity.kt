package com.chenge.markdownsdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.chenge.markdown.engine.MarkdownEngine
import com.chenge.markdown.engine.MarkdownLoader
import com.chenge.markdown.engine.MarkdownParser
import com.chenge.markdown.engine.MarkdownView

import kotlinx.coroutines.delay

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
    var renderTime by remember { mutableStateOf("未开始") }
    var isRendering by remember { mutableStateOf(false) }
    var streamProgress by remember { mutableStateOf(0f) }
    var testCaseIndex by remember { mutableStateOf(0) }
    
    // 测试用例
    val testCases = listOf(
        """# 基础语法测试
这是一个基础的Markdown语法测试用例。

## 文本样式
**粗体文本** 和 *斜体文本* 以及 ***粗斜体文本***

~~删除线文本~~ 和 `行内代码`

## 引用
> 这是一个引用块
> 
> 可以包含多行内容
> > 嵌套引用

## 分割线
---

## 链接和图片
[GitHub链接](https://github.com)

![示例图片](https://via.placeholder.com/150)""",
        
        """# 高级功能测试

## 有序列表
1. 第一项
2. 第二项
   1. 子项目 2.1
   2. 子项目 2.2
3. 第三项

## 无序列表
- 项目 A
- 项目 B
  - 子项目 B.1
  - 子项目 B.2
    - 深层子项目
- 项目 C

## 任务列表
- [x] 已完成任务
- [ ] 未完成任务
- [x] 另一个已完成任务

## 代码块
```kotlin
class MarkdownDemo {
    fun renderMarkdown(content: String) {
        println("渲染内容: " + content)
    }
    
    companion object {
        const val VERSION = "1.0.0"
    }
}
```

## 表格
| 功能 | 支持状态 | 备注 |
|------|----------|------|
| 粗体 | ✅ | 完全支持 |
| 斜体 | ✅ | 完全支持 |
| 代码块 | ✅ | 语法高亮 |
| 表格 | ✅ | 自适应宽度 |
| 图片 | ✅ | 支持缩放 |""",
        
        """# 性能与流式渲染测试

## 长文本性能测试
""" + "这是一个用于测试流式渲染性能的长段落。包含了丰富的Markdown语法元素，用来验证在大量内容下的渲染效果和性能表现。".repeat(10) + """

## 复杂嵌套结构
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题

### 复杂列表结构
1. **主要功能**
   - 基础渲染
     - 文本样式
     - 链接处理
   - 高级功能
     - 表格渲染
     - 代码高亮
     - 数学公式
2. **性能优化**
   - 异步渲染
   - 流式处理
   - 内存管理

### 数学公式测试
行内公式: E = mc²

块级公式:
∑(i=1 to n) x_i = x_1 + x_2 + ... + x_n

### 多种代码语言
```python
def quick_sort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quick_sort(left) + middle + quick_sort(right)
```

```sql
SELECT u.name, COUNT(p.id) as post_count
FROM users u
LEFT JOIN posts p ON u.id = p.user_id
WHERE u.active = 1
GROUP BY u.id
ORDER BY post_count DESC;
```

### 复杂表格
| 序号 | 功能模块 | 实现状态 | 优先级 | 负责人 | 预计完成时间 |
|------|----------|----------|--------|--------|-------------|
| 1 | 基础渲染引擎 | ✅ 已完成 | 高 | 张三 | 2024-01-15 |
| 2 | 流式渲染 | 🔄 进行中 | 高 | 李四 | 2024-01-20 |
| 3 | 性能优化 | ⏳ 计划中 | 中 | 王五 | 2024-01-25 |
| 4 | 插件系统 | ⏳ 计划中 | 低 | 赵六 | 2024-02-01 |

---

> **注意**: 这个测试用例包含了大量内容，专门用于测试流式渲染的性能和效果。"""
    )
    
    // 加载示例 Markdown 内容
    val markdownContent = remember(testCaseIndex) {
        if (testCaseIndex < testCases.size) {
            testCases[testCaseIndex]
        } else {
            MarkdownLoader.loadFromAssets(context, "full_format_test.md")
        }
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Markdown SDK 演示",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 渲染设置卡片
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "渲染设置",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 测试用例选择
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "测试用例",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    testCases.forEachIndexed { index, _ ->
                                        FilterChip(
                                            onClick = { testCaseIndex = index },
                                            label = { Text("用例${index + 1}") },
                                            selected = testCaseIndex == index
                                        )
                                    }
                                    FilterChip(
                                        onClick = { testCaseIndex = testCases.size },
                                        label = { Text("完整测试") },
                                        selected = testCaseIndex == testCases.size
                                    )
                                }
                            }
                        }

                        // 异步渲染开关
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isAsyncMode) "异步渲染" else "同步渲染",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isAsyncMode) "启用后台渲染模式" else "使用主线程渲染",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAsyncMode,
                                    onCheckedChange = { isAsyncMode = it }
                                )
                            }
                        }

                        // 流式渲染开关
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isStreamMode) "流式渲染" else "普通渲染",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isStreamMode) "实时显示渲染进度" else "一次性完成渲染",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isStreamMode,
                                    onCheckedChange = { isStreamMode = it }
                                )
                            }
                        }

                        // 渲染按钮
                        Button(
                            onClick = {
                                isRendering = true
                                renderTime = "渲染中..."
                            },
                            enabled = !isRendering,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isRendering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = "开始渲染",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // 流式渲染进度
                        if (isStreamMode && isRendering) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "流式渲染进度",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = streamProgress,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "${(streamProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // 性能指标
                        if (renderTime != "未开始") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "性能指标",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = "渲染时间: $renderTime",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Markdown 内容显示
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "渲染结果",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        var markdownView by remember { mutableStateOf<MarkdownView?>(null) }
                        
                        // 渲染效果处理
                        LaunchedEffect(isRendering, markdownView) {
                            if (isRendering && markdownView != null) {
                                val startTime = System.currentTimeMillis()
                                
                                if (isStreamMode) {
                                    // 流式渲染模拟
                                    for (i in 1..10) {
                                        streamProgress = i / 10f
                                        delay(100)
                                    }
                                } else {
                                    delay(500) // 模拟渲染时间
                                }
                                
                                // 实际渲染
                                engine.render(markdownView!!, parsedMarkdown)
                                
                                val endTime = System.currentTimeMillis()
                                renderTime = "${endTime - startTime}ms"
                                isRendering = false
                                streamProgress = 0f
                            }
                        }
                        
                        AndroidView(
                            factory = { context ->
                                MarkdownView(context).apply {
                                    // 初始化 MarkdownView
                                    setPadding(16, 16, 16, 16)
                                    // 启用滚动功能
                                    movementMethod = android.text.method.ScrollingMovementMethod()
                                    // 禁用滚动条以避免ScrollBarDrawable空指针异常
                                    isVerticalScrollBarEnabled = false
                                    isHorizontalScrollBarEnabled = false
                                    // 禁用嵌套滚动，让 LazyColumn 处理滚动
                                    isNestedScrollingEnabled = false
                                    overScrollMode = android.view.View.OVER_SCROLL_NEVER
                                    // 禁用触摸反馈以避免滚动冲突
                                    isClickable = false
                                    isFocusable = false
                                    // 移除背景以避免触摸事件干扰
                                    background = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) { view ->
                            markdownView = view
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
