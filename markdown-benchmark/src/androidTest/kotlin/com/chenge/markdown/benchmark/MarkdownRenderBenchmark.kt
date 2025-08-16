package com.chenge.markdown.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark 测试用例：验证 Markdown 渲染性能
 * 包含同步/异步/流式渲染的性能对比
 */
@RunWith(AndroidJUnit4::class)
class MarkdownRenderBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val sampleMarkdown = """
        # 标题测试
        
        这是一个**粗体**和*斜体*的测试文档。
        
        ## 列表测试
        
        - [ ] 任务列表项 1
        - [x] 已完成任务
        - [ ] 任务列表项 3
        
        ### 表格测试
        
        | 列1 | 列2 | 列3 |
        |-----|-----|-----|
        | 数据1 | 数据2 | 数据3 |
        | 数据4 | 数据5 | 数据6 |
        
        ```kotlin
        fun example() {
            println("代码块测试")
        }
        ```
        
        ![图片](https://example.com/image.png)
        
        [链接测试](https://example.com)
    """.trimIndent()

    @Test
    fun benchmarkSyncRender() {
        benchmarkRule.measureRepeated {
            // TODO: 实现同步渲染性能测试
            // MarkdownEngine.renderSync(sampleMarkdown)
        }
    }

    @Test
    fun benchmarkAsyncRender() {
        benchmarkRule.measureRepeated {
            // TODO: 实现异步渲染性能测试
            // MarkdownEngine.renderAsync(sampleMarkdown)
        }
    }

    @Test
    fun benchmarkStreamRender() {
        benchmarkRule.measureRepeated {
            // TODO: 实现流式渲染性能测试
            // MarkdownEngine.renderStream(sampleMarkdown)
        }
    }

    @Test
    fun benchmarkParseOnly() {
        benchmarkRule.measureRepeated {
            // TODO: 实现纯解析性能测试
            // MarkdownParser.parse(sampleMarkdown)
        }
    }

    @Test
    fun benchmarkLargeDocument() {
        val largeMarkdown = sampleMarkdown.repeat(100)
        benchmarkRule.measureRepeated {
            // TODO: 实现大文档渲染性能测试
            // MarkdownEngine.renderSync(largeMarkdown)
        }
    }
}