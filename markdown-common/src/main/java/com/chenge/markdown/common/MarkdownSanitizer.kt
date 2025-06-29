package com.chenge.markdown.common

/**
 * MarkdownSanitizer：简单净化Markdown内容
 */
object MarkdownSanitizer {

  /**
   * 过滤危险HTML标签
   */
  fun sanitize(input: String): String {
    return input
      .replace("<script", "&lt;script")
      .replace("</script>", "&lt;/script&gt;")
      .replace("<iframe", "&lt;iframe")
      .replace("</iframe>", "&lt;/iframe&gt;")
  }
}