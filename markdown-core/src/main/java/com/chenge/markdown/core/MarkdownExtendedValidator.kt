package com.chenge.markdown.core

/**
 * Markdown 扩展语法验证器
 * 验证 GFM 表格、任务列表、嵌套块等扩展语法支持
 */
class MarkdownExtendedValidator {
    
    /**
     * 扩展语法验证结果
     */
    data class ExtendedValidationResult(
        val isValid: Boolean,
        val supportedFeatures: List<String>,
        val failedFeatures: List<String>,
        val details: Map<String, Any>
    )
    
    /**
     * 扩展语法特性枚举
     */
    enum class ExtendedSyntaxFeature(val displayName: String, val testContent: String) {
        TABLES("GFM表格", """
            | 列1 | 列2 | 列3 |
            |-----|:---:|----:|
            | 左对齐 | 居中 | 右对齐 |
            | 数据1 | 数据2 | 数据3 |
        """.trimIndent()),
        
        TASK_LISTS("任务列表", """
            - [x] 已完成任务
            - [ ] 未完成任务
            - [X] 大写X任务
              - [ ] 嵌套任务
        """.trimIndent()),
        
        NESTED_BLOCKQUOTES("嵌套引用", """
            > 一级引用
            > > 二级引用
            > > > 三级引用
            > 回到一级
        """.trimIndent()),
        
        NESTED_LISTS("嵌套列表", """
            - 一级列表
              - 二级列表
                - 三级列表
            1. 有序列表
               1. 嵌套有序
        """.trimIndent()),
        
        STRIKETHROUGH("删除线", "这是~~删除的文本~~和正常文本"),
        
        AUTO_LINKS("自动链接", "访问 https://github.com 或联系 user@example.com")
    }
    
    /**
     * 验证所有扩展语法
     */
    fun validateExtendedSyntax(): ExtendedValidationResult {
        val supportedFeatures = mutableListOf<String>()
        val failedFeatures = mutableListOf<String>()
        val details = mutableMapOf<String, Any>()
        
        ExtendedSyntaxFeature.values().forEach { feature ->
            try {
                val isSupported = when (feature) {
                    ExtendedSyntaxFeature.TABLES -> validateTables(feature.testContent)
                    ExtendedSyntaxFeature.TASK_LISTS -> validateTaskLists(feature.testContent)
                    ExtendedSyntaxFeature.NESTED_BLOCKQUOTES -> validateNestedBlockquotes(feature.testContent)
                    ExtendedSyntaxFeature.NESTED_LISTS -> validateNestedLists(feature.testContent)
                    ExtendedSyntaxFeature.STRIKETHROUGH -> validateStrikethrough(feature.testContent)
                    ExtendedSyntaxFeature.AUTO_LINKS -> validateAutoLinks(feature.testContent)
                }
                
                if (isSupported) {
                    supportedFeatures.add(feature.displayName)
                } else {
                    failedFeatures.add(feature.displayName)
                }
                
                details[feature.displayName] = isSupported
            } catch (e: Exception) {
                failedFeatures.add(feature.displayName)
                details[feature.displayName] = "错误: ${e.message}"
            }
        }
        
        return ExtendedValidationResult(
            isValid = failedFeatures.isEmpty(),
            supportedFeatures = supportedFeatures,
            failedFeatures = failedFeatures,
            details = details
        )
    }
    
    /**
     * 验证表格语法
     */
    private fun validateTables(testContent: String): Boolean {
        val lines = testContent.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        // 检查是否有表格行和分隔符
        var hasTableRow = false
        var hasSeparator = false
        
        for (i in lines.indices) {
            if (MarkdownExtendedSyntax.Tables.isTableRow(lines[i])) {
                hasTableRow = true
                // 检查下一行是否为分隔符
                if (i + 1 < lines.size && MarkdownExtendedSyntax.Tables.isTableSeparator(lines[i + 1])) {
                    hasSeparator = true
                    
                    // 验证表格解析
                    val headerCells = MarkdownExtendedSyntax.Tables.parseTableRow(lines[i])
                    val alignments = MarkdownExtendedSyntax.Tables.parseTableAlignment(lines[i + 1])
                    
                    return headerCells.isNotEmpty() && alignments.isNotEmpty() && 
                           headerCells.size == alignments.size
                }
            }
        }
        
        return hasTableRow && hasSeparator && MarkdownExtendedSyntax.Tables.hasTables(testContent)
    }
    
    /**
     * 验证任务列表语法
     */
    private fun validateTaskLists(testContent: String): Boolean {
        val lines = testContent.split("\n").filter { it.trim().isNotEmpty() }
        
        var hasCompletedTask = false
        var hasUncompletedTask = false
        var hasNestedTask = false
        
        for (line in lines) {
            if (MarkdownExtendedSyntax.TaskLists.isTaskListItem(line)) {
                if (MarkdownExtendedSyntax.TaskLists.isTaskCompleted(line)) {
                    hasCompletedTask = true
                } else {
                    hasUncompletedTask = true
                }
                
                if (MarkdownExtendedSyntax.TaskLists.getTaskIndentLevel(line) > 0) {
                    hasNestedTask = true
                }
            }
        }
        
        return hasCompletedTask && hasUncompletedTask && 
               MarkdownExtendedSyntax.TaskLists.hasTaskLists(testContent)
    }
    
    /**
     * 验证嵌套引用语法
     */
    private fun validateNestedBlockquotes(testContent: String): Boolean {
        val lines = testContent.split("\n").filter { it.trim().isNotEmpty() }
        
        var maxLevel = 0
        var hasMultipleLevels = false
        
        for (line in lines) {
            if (MarkdownExtendedSyntax.NestedBlocks.isNestedBlockquote(line)) {
                val level = MarkdownExtendedSyntax.NestedBlocks.getNestedQuoteLevel(line)
                maxLevel = maxOf(maxLevel, level)
                if (level > 1) {
                    hasMultipleLevels = true
                }
            }
        }
        
        return maxLevel >= 3 && hasMultipleLevels && 
               MarkdownExtendedSyntax.NestedBlocks.hasNestedBlocks(testContent)
    }
    
    /**
     * 验证嵌套列表语法
     */
    private fun validateNestedLists(testContent: String): Boolean {
        val lines = testContent.split("\n").filter { it.trim().isNotEmpty() }
        
        var hasNestedUnordered = false
        var hasNestedOrdered = false
        
        for (line in lines) {
            if (MarkdownExtendedSyntax.NestedBlocks.isNestedList(line)) {
                val level = MarkdownExtendedSyntax.NestedBlocks.getNestedListLevel(line)
                if (level > 0) {
                    val trimmed = line.trim()
                    if (MarkdownSyntax.Lists.isUnorderedList(trimmed)) {
                        hasNestedUnordered = true
                    } else if (MarkdownSyntax.Lists.isOrderedList(trimmed)) {
                        hasNestedOrdered = true
                    }
                }
            }
        }
        
        return hasNestedUnordered && hasNestedOrdered
    }
    
    /**
     * 验证删除线语法
     */
    private fun validateStrikethrough(testContent: String): Boolean {
        val hasStrikethrough = MarkdownExtendedSyntax.Strikethrough.hasStrikethrough(testContent)
        val extractedText = MarkdownExtendedSyntax.Strikethrough.extractStrikethroughText(testContent)
        
        return hasStrikethrough && extractedText.isNotEmpty() && 
               extractedText.any { it.isNotBlank() }
    }
    
    /**
     * 验证自动链接语法
     */
    private fun validateAutoLinks(testContent: String): Boolean {
        val hasAutoLinks = MarkdownExtendedSyntax.AutoLinks.hasAutoLinks(testContent)
        val urls = MarkdownExtendedSyntax.AutoLinks.extractUrls(testContent)
        val emails = MarkdownExtendedSyntax.AutoLinks.extractEmails(testContent)
        
        return hasAutoLinks && (urls.isNotEmpty() || emails.isNotEmpty())
    }
    
    /**
     * 生成扩展语法验证报告
     */
    fun generateExtendedValidationReport(): String {
        val result = validateExtendedSyntax()
        
        return buildString {
            appendLine("=== Markdown 扩展语法验证报告 ===")
            appendLine()
            appendLine("验证状态: ${if (result.isValid) "✅ 通过" else "❌ 失败"}")
            appendLine("支持特性数量: ${result.supportedFeatures.size}/${ExtendedSyntaxFeature.values().size}")
            appendLine()
            
            if (result.supportedFeatures.isNotEmpty()) {
                appendLine("✅ 支持的扩展特性:")
                result.supportedFeatures.forEach { feature ->
                    appendLine("  - $feature")
                }
                appendLine()
            }
            
            if (result.failedFeatures.isNotEmpty()) {
                appendLine("❌ 不支持的扩展特性:")
                result.failedFeatures.forEach { feature ->
                    appendLine("  - $feature")
                }
                appendLine()
            }
            
            appendLine("详细信息:")
            result.details.forEach { (feature, status) ->
                appendLine("  $feature: $status")
            }
            
            appendLine()
            appendLine("验证时间: ${System.currentTimeMillis()}")
        }
    }
    
    /**
     * 测试特定扩展语法特性
     */
    fun testSpecificFeature(feature: ExtendedSyntaxFeature): Boolean {
        return when (feature) {
            ExtendedSyntaxFeature.TABLES -> validateTables(feature.testContent)
            ExtendedSyntaxFeature.TASK_LISTS -> validateTaskLists(feature.testContent)
            ExtendedSyntaxFeature.NESTED_BLOCKQUOTES -> validateNestedBlockquotes(feature.testContent)
            ExtendedSyntaxFeature.NESTED_LISTS -> validateNestedLists(feature.testContent)
            ExtendedSyntaxFeature.STRIKETHROUGH -> validateStrikethrough(feature.testContent)
            ExtendedSyntaxFeature.AUTO_LINKS -> validateAutoLinks(feature.testContent)
        }
    }
    
    /**
     * 获取扩展语法统计信息
     */
    fun getExtendedSyntaxStats(text: String): Map<String, Int> {
        val info = MarkdownExtendedSyntax.analyzeExtendedSyntax(text)
        
        return mapOf(
            "表格数量" to info.tableCount,
            "任务列表数量" to info.taskCount,
            "嵌套块数量" to info.nestedBlockCount,
            "删除线数量" to MarkdownExtendedSyntax.Strikethrough.extractStrikethroughText(text).size,
            "自动链接数量" to (MarkdownExtendedSyntax.AutoLinks.extractUrls(text).size + 
                              MarkdownExtendedSyntax.AutoLinks.extractEmails(text).size)
        )
    }
}