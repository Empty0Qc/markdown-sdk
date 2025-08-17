package com.chenge.markdown.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chenge.markdown.compose.ui.screen.MarkdownDemoScreen
import com.chenge.markdown.compose.ui.theme.MarkdownComposeTheme

/**
 * Compose 版本的 Markdown 渲染示例应用
 * 
 * 这个 Activity 展示了如何在 Jetpack Compose 中集成和使用 Markdown SDK
 * 包含以下功能演示：
 * - 基本 Markdown 渲染
 * - 异步渲染
 * - 自定义配置
 * - 性能测试
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MarkdownComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        MarkdownDemoScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
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