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
  val enableTaskList: Boolean = true
)