package com.chenge.markdown.common

/**
 * ✅ 在渲染前对 Markdown 内容进行“净化”处理，避免XSS/恶意HTML
 * ✅ 提供一个MarkdownConfig配置类，可控制：
 *
 * 是否允许HTML
 *
 * 是否启用表格/任务清单等插件
 *
 * 是否使用自定义图片加载
 *
 * ✅ 给上层App暴露统一入口
 */

/**
 * MarkdownConfig：SDK全局配置
 */
data class MarkdownConfig(
  val enableHtml: Boolean = false,
  val enableTables: Boolean = true,
  val enableTaskList: Boolean = true,
  val enableLatex: Boolean = false,
  val enableImageLoading: Boolean = true,
  val enableLinkClick: Boolean = true,
  val maxImageWidth: Int = 800,
  val maxImageHeight: Int = 600,
  val asyncRendering: Boolean = false,
  val debugMode: Boolean = false,
  val customPlugins: List<String> = emptyList()
) {
  companion object {
    /**
     * 默认配置
     */
    fun default() = MarkdownConfig()
    
    /**
     * 完整功能配置
     */
    fun full() = MarkdownConfig(
      enableHtml = true,
      enableTables = true,
      enableTaskList = true,
      enableLatex = true,
      enableImageLoading = true,
      enableLinkClick = true
    )
    
    /**
     * 安全模式配置（禁用 HTML）
     */
    fun safe() = MarkdownConfig(
      enableHtml = false,
      enableTables = true,
      enableTaskList = true,
      enableLatex = false
    )
    
    /**
     * 性能优化配置
     */
    fun performance() = MarkdownConfig(
      enableHtml = false,
      enableTables = false,
      enableTaskList = false,
      enableLatex = false,
      enableImageLoading = false,
      asyncRendering = true
    )
    
    /**
     * 博客/文档配置（支持表格、任务列表、LaTeX）
     */
    fun blog() = MarkdownConfig(
      enableHtml = false,
      enableTables = true,
      enableTaskList = true,
      enableLatex = true,
      enableImageLoading = true,
      enableLinkClick = true
    )
    
    /**
     * 聊天/消息配置（轻量级，快速渲染）
     */
    fun chat() = MarkdownConfig(
      enableHtml = false,
      enableTables = false,
      enableTaskList = false,
      enableLatex = false,
      enableImageLoading = true,
      enableLinkClick = true,
      asyncRendering = true
    )
    
    /**
     * 富文本编辑器配置（支持所有功能）
     */
    fun editor() = MarkdownConfig(
      enableHtml = true,
      enableTables = true,
      enableTaskList = true,
      enableLatex = true,
      enableImageLoading = true,
      enableLinkClick = true,
      debugMode = true
    )
  }
}

/**
 * MarkdownConfig DSL 构建器
 */
class MarkdownConfigBuilder {
  var enableHtml: Boolean = false
  var enableTables: Boolean = true
  var enableTaskList: Boolean = true
  var enableLatex: Boolean = false
  var enableImageLoading: Boolean = true
  var enableLinkClick: Boolean = true
  var maxImageWidth: Int = 800
  var maxImageHeight: Int = 600
  var asyncRendering: Boolean = false
  var debugMode: Boolean = false
  var customPlugins: MutableList<String> = mutableListOf()
  
  fun plugin(name: String) {
    customPlugins.add(name)
  }
  
  fun imageSize(width: Int, height: Int) {
    maxImageWidth = width
    maxImageHeight = height
  }
  
  /**
   * 启用所有功能
   */
  fun enableAll() {
    enableHtml = true
    enableTables = true
    enableTaskList = true
    enableLatex = true
    enableImageLoading = true
    enableLinkClick = true
  }
  
  /**
   * 禁用所有功能（最小配置）
   */
  fun disableAll() {
    enableHtml = false
    enableTables = false
    enableTaskList = false
    enableLatex = false
    enableImageLoading = false
    enableLinkClick = false
  }
  
  /**
   * 启用异步渲染
   */
  fun async() {
    asyncRendering = true
  }
  
  /**
   * 启用调试模式
   */
  fun debug() {
    debugMode = true
  }
  
  /**
   * 安全模式（禁用 HTML）
   */
  fun safeMode() {
    enableHtml = false
  }
  
  /**
   * 启用表格支持
   */
  fun tables() {
    enableTables = true
  }
  
  /**
   * 启用任务列表支持
   */
  fun taskLists() {
    enableTaskList = true
  }
  
  /**
   * 启用 LaTeX 支持
   */
  fun latex() {
    enableLatex = true
  }
  
  fun build(): MarkdownConfig {
    return MarkdownConfig(
      enableHtml = enableHtml,
      enableTables = enableTables,
      enableTaskList = enableTaskList,
      enableLatex = enableLatex,
      enableImageLoading = enableImageLoading,
      enableLinkClick = enableLinkClick,
      maxImageWidth = maxImageWidth,
      maxImageHeight = maxImageHeight,
      asyncRendering = asyncRendering,
      debugMode = debugMode,
      customPlugins = customPlugins.toList()
    )
  }
}

/**
 * DSL 函数用于创建 MarkdownConfig
 */
fun markdownConfig(block: MarkdownConfigBuilder.() -> Unit): MarkdownConfig {
  return MarkdownConfigBuilder().apply(block).build()
}