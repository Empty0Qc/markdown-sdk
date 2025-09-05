package com.chenge.markdown.core

/**
 * Markdown 扩展语法支持
 * 包括 GFM 表格、任务列表、嵌套块等扩展语法
 */
object MarkdownExtendedSyntax {
    
    /**
     * 表格语法支持
     */
    object Tables {
        // 表格行分隔符模式
        private val TABLE_SEPARATOR_PATTERN = Regex("^\\s*\\|?\\s*:?-+:?\\s*(\\|\\s*:?-+:?\\s*)*\\|?\\s*$")
        
        // 表格行模式
        private val TABLE_ROW_PATTERN = Regex("^\\s*\\|.*\\|\\s*$")
        
        // 表格单元格分割模式
        private val TABLE_CELL_PATTERN = Regex("\\|")
        
        /**
         * 检查是否为表格分隔符行
         */
        fun isTableSeparator(line: String): Boolean {
            return TABLE_SEPARATOR_PATTERN.matches(line.trim())
        }
        
        /**
         * 检查是否为表格行
         */
        fun isTableRow(line: String): Boolean {
            val trimmed = line.trim()
            return trimmed.contains("|") && 
                   (trimmed.startsWith("|") || trimmed.endsWith("|") || trimmed.count { it == '|' } >= 2)
        }
        
        /**
         * 解析表格行为单元格
         */
        fun parseTableRow(line: String): List<String> {
            val trimmed = line.trim()
            val withoutBorders = if (trimmed.startsWith("|")) {
                trimmed.substring(1)
            } else trimmed
            
            val finalLine = if (withoutBorders.endsWith("|")) {
                withoutBorders.substring(0, withoutBorders.length - 1)
            } else withoutBorders
            
            return finalLine.split("|").map { it.trim() }
        }
        
        /**
         * 解析表格对齐方式
         */
        fun parseTableAlignment(separatorLine: String): List<TableAlignment> {
            val cells = parseTableRow(separatorLine)
            return cells.map { cell ->
                when {
                    cell.startsWith(":") && cell.endsWith(":") -> TableAlignment.CENTER
                    cell.startsWith(":") -> TableAlignment.LEFT
                    cell.endsWith(":") -> TableAlignment.RIGHT
                    else -> TableAlignment.DEFAULT
                }
            }
        }
        
        /**
         * 检查文本是否包含表格
         */
        fun hasTables(text: String): Boolean {
            val lines = text.split("\n")
            for (i in lines.indices) {
                if (isTableRow(lines[i])) {
                    // 检查下一行是否为分隔符
                    if (i + 1 < lines.size && isTableSeparator(lines[i + 1])) {
                        return true
                    }
                }
            }
            return false
        }
    }
    
    /**
     * 表格对齐方式
     */
    enum class TableAlignment {
        DEFAULT, LEFT, CENTER, RIGHT
    }
    
    /**
     * 任务列表语法支持
     */
    object TaskLists {
        // 任务列表模式
        private val TASK_LIST_PATTERN = Regex("^(\\s*)[-*+]\\s+\\[([ xX])\\]\\s+(.*)$")
        
        // 简单任务列表检查模式
        private val SIMPLE_TASK_PATTERN = Regex("[-*+]\\s+\\[([ xX])\\]")
        
        /**
         * 检查是否为任务列表项
         */
        fun isTaskListItem(line: String): Boolean {
            return TASK_LIST_PATTERN.matches(line)
        }
        
        /**
         * 检查任务是否已完成
         */
        fun isTaskCompleted(line: String): Boolean {
            val match = TASK_LIST_PATTERN.find(line)
            return match?.groupValues?.get(2)?.lowercase() == "x"
        }
        
        /**
         * 获取任务文本内容
         */
        fun getTaskText(line: String): String? {
            val match = TASK_LIST_PATTERN.find(line)
            return match?.groupValues?.get(3)
        }
        
        /**
         * 获取任务缩进级别
         */
        fun getTaskIndentLevel(line: String): Int {
            val match = TASK_LIST_PATTERN.find(line)
            val indent = match?.groupValues?.get(1) ?: ""
            return indent.length / 2 // 假设每2个空格为一级缩进
        }
        
        /**
         * 检查文本是否包含任务列表
         */
        fun hasTaskLists(text: String): Boolean {
            return SIMPLE_TASK_PATTERN.containsMatchIn(text)
        }
        
        /**
         * 解析任务列表项
         */
        fun parseTaskItem(line: String): TaskItem? {
            val match = TASK_LIST_PATTERN.find(line) ?: return null
            val indent = match.groupValues[1].length / 2
            val isCompleted = match.groupValues[2].lowercase() == "x"
            val text = match.groupValues[3]
            return TaskItem(indent, isCompleted, text)
        }
    }
    
    /**
     * 任务列表项数据类
     */
    data class TaskItem(
        val indentLevel: Int,
        val isCompleted: Boolean,
        val text: String
    )
    
    /**
     * 嵌套块语法支持
     */
    object NestedBlocks {
        /**
         * 检查是否为嵌套引用块
         */
        fun isNestedBlockquote(line: String): Boolean {
            val trimmed = line.trim()
            return trimmed.startsWith(">") && trimmed.count { it == '>' } > 1
        }
        
        /**
         * 获取嵌套引用级别
         */
        fun getNestedQuoteLevel(line: String): Int {
            val trimmed = line.trim()
            var level = 0
            var i = 0
            while (i < trimmed.length && trimmed[i] == '>') {
                level++
                i++
                // 跳过可选的空格
                if (i < trimmed.length && trimmed[i] == ' ') {
                    i++
                }
            }
            return level
        }
        
        /**
         * 获取嵌套引用内容
         */
        fun getNestedQuoteContent(line: String): String {
            val trimmed = line.trim()
            var i = 0
            while (i < trimmed.length && trimmed[i] == '>') {
                i++
                // 跳过可选的空格
                if (i < trimmed.length && trimmed[i] == ' ') {
                    i++
                }
            }
            return if (i < trimmed.length) trimmed.substring(i) else ""
        }
        
        /**
         * 检查是否为嵌套列表
         */
        fun isNestedList(line: String): Boolean {
            val trimmed = line.trim()
            val leadingSpaces = line.length - line.trimStart().length
            return leadingSpaces >= 2 && (MarkdownSyntax.Lists.isUnorderedList(trimmed) || 
                                         MarkdownSyntax.Lists.isOrderedList(trimmed))
        }
        
        /**
         * 获取嵌套列表级别
         */
        fun getNestedListLevel(line: String): Int {
            val leadingSpaces = line.length - line.trimStart().length
            return leadingSpaces / 2 // 每2个空格为一级缩进
        }
        
        /**
         * 检查文本是否包含嵌套块
         */
        fun hasNestedBlocks(text: String): Boolean {
            return text.split("\n").any { line ->
                isNestedBlockquote(line) || isNestedList(line)
            }
        }
    }
    
    /**
     * 删除线语法支持
     */
    object Strikethrough {
        // 删除线模式
        val STRIKETHROUGH_PATTERN = Regex("~~([^~]+)~~")
        
        /**
         * 检查文本是否包含删除线
         */
        fun hasStrikethrough(text: String): Boolean {
            return STRIKETHROUGH_PATTERN.containsMatchIn(text)
        }
        
        /**
         * 提取删除线文本
         */
        fun extractStrikethroughText(text: String): List<String> {
            return STRIKETHROUGH_PATTERN.findAll(text)
                .map { it.groupValues[1] }
                .toList()
        }
    }
    
    /**
     * 自动链接语法支持
     */
    object AutoLinks {
        // URL 自动链接模式
        private val URL_PATTERN = Regex("https?://[^\\s<>\"]+[^\\s<>\".,;:!?]")
        
        // Email 自动链接模式
        private val EMAIL_PATTERN = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        
        /**
         * 检查文本是否包含自动链接
         */
        fun hasAutoLinks(text: String): Boolean {
            return URL_PATTERN.containsMatchIn(text) || EMAIL_PATTERN.containsMatchIn(text)
        }
        
        /**
         * 提取所有URL
         */
        fun extractUrls(text: String): List<String> {
            return URL_PATTERN.findAll(text).map { it.value }.toList()
        }
        
        /**
         * 提取所有Email
         */
        fun extractEmails(text: String): List<String> {
            return EMAIL_PATTERN.findAll(text).map { it.value }.toList()
        }
    }
    
    /**
     * 检查文本是否包含任何扩展语法
     */
    fun hasExtendedSyntax(text: String): Boolean {
        return Tables.hasTables(text) ||
               TaskLists.hasTaskLists(text) ||
               NestedBlocks.hasNestedBlocks(text) ||
               Strikethrough.hasStrikethrough(text) ||
               AutoLinks.hasAutoLinks(text)
    }
    
    /**
     * 扩展语法信息
     */
    data class ExtendedSyntaxInfo(
        val hasTables: Boolean = false,
        val hasTaskLists: Boolean = false,
        val hasNestedBlocks: Boolean = false,
        val hasStrikethrough: Boolean = false,
        val hasAutoLinks: Boolean = false,
        val tableCount: Int = 0,
        val taskCount: Int = 0,
        val nestedBlockCount: Int = 0
    )
    
    /**
     * 分析扩展语法
     */
    fun analyzeExtendedSyntax(text: String): ExtendedSyntaxInfo {
        val lines = text.split("\n")
        var tableCount = 0
        var taskCount = 0
        var nestedBlockCount = 0
        
        // 统计表格数量
        var inTable = false
        for (i in lines.indices) {
            if (Tables.isTableRow(lines[i]) && i + 1 < lines.size && Tables.isTableSeparator(lines[i + 1])) {
                if (!inTable) {
                    tableCount++
                    inTable = true
                }
            } else if (!Tables.isTableRow(lines[i]) && !Tables.isTableSeparator(lines[i])) {
                inTable = false
            }
        }
        
        // 统计任务列表数量
        taskCount = lines.count { TaskLists.isTaskListItem(it) }
        
        // 统计嵌套块数量
        nestedBlockCount = lines.count { NestedBlocks.isNestedBlockquote(it) || NestedBlocks.isNestedList(it) }
        
        return ExtendedSyntaxInfo(
            hasTables = Tables.hasTables(text),
            hasTaskLists = TaskLists.hasTaskLists(text),
            hasNestedBlocks = NestedBlocks.hasNestedBlocks(text),
            hasStrikethrough = Strikethrough.hasStrikethrough(text),
            hasAutoLinks = AutoLinks.hasAutoLinks(text),
            tableCount = tableCount,
            taskCount = taskCount,
            nestedBlockCount = nestedBlockCount
        )
    }
}