package com.chenge.markdown.common

/**
 * 代码高亮实现方式
 */
enum class CodeHighlight {
    /** 不启用代码高亮 */
    NONE,
    /** 简单关键字高亮 */
    SIMPLE,
    /** Prism4j 高亮 */
    PRISM_LIGHT,
    /** Prism4j 深色主题高亮 */
    PRISM_DARK
}
