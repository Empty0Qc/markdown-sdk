package com.chenge.markdown.core

import com.chenge.markdown.common.MarkdownSanitizer

/**
 * MarkdownParser: 基础解析器 (目前直接返回输入)
 */
object MarkdownParser {

  /**
   * 解析Markdown文本
   * @param input 原始Markdown字符串
   * @return 暂时直接返回原始字符串
   */
  fun parse(input: String): String {
    // Emoji 替换
    val markdownWithEmoji = EmojiReplacer.replaceShortcodes(input)
    val safeMarkdown = MarkdownSanitizer.sanitize(markdownWithEmoji)
    return safeMarkdown
  }
}
