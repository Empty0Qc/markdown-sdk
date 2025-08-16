package com.chenge.markdown.core

import org.junit.Test
import org.junit.Assert.*

/**
 * MarkdownParser 单元测试
 */
class MarkdownParserTest {

    @Test
    fun `test basic parsing with emoji replacement`() {
        val input = "# Hello :smile: World"
        val result = MarkdownParser.parse(input)
        assertTrue("Should contain emoji", result.contains("😄"))
        assertTrue("Should contain header", result.contains("# Hello"))
    }

    @Test
    fun `test parsing with dangerous HTML`() {
        val input = "Hello <script>alert('xss')</script> world"
        val result = MarkdownParser.parse(input)
        assertFalse("Should not contain script tag", result.contains("<script>"))
        assertTrue("Should contain escaped script", result.contains("&lt;script"))
    }

    @Test
    fun `test parsing with emoji and HTML sanitization`() {
        val input = ":smile: <script>alert('test')</script> :heart:"
        val result = MarkdownParser.parse(input)
        assertTrue("Should contain smile emoji", result.contains("😄"))
        assertTrue("Should contain heart emoji", result.contains("❤️"))
        assertFalse("Should not contain script tag", result.contains("<script>"))
        assertTrue("Should contain escaped script", result.contains("&lt;script"))
    }

    @Test
    fun `test empty input`() {
        val input = ""
        val result = MarkdownParser.parse(input)
        assertEquals("", result)
    }

    @Test
    fun `test whitespace only input`() {
        val input = "   \n\t  "
        val result = MarkdownParser.parse(input)
        assertEquals(input, result)
    }

    @Test
    fun `test complex markdown with emojis and HTML`() {
        val input = """# Title :smile:
            |
            |**Bold text** with :heart: emoji
            |
            |<iframe src="evil.com"></iframe>
            |
            |- List item :thumbsup:
            |  - Nested item :wink:
            |
            |```kotlin
            |fun test() {
            |    println(":joy:")
            |}
            |```
            |
            |<script>alert('xss')</script>
        """.trimMargin()
        
        val result = MarkdownParser.parse(input)
        
        // Check emoji replacement
        assertTrue("Should contain smile emoji", result.contains("😄"))
        assertTrue("Should contain heart emoji", result.contains("❤️"))
        assertTrue("Should contain thumbsup emoji", result.contains("👍"))
        assertTrue("Should contain wink emoji", result.contains("😉"))
        
        // Check HTML sanitization
        assertFalse("Should not contain script tag", result.contains("<script>"))
        assertFalse("Should not contain iframe tag", result.contains("<iframe"))
        assertTrue("Should contain escaped script", result.contains("&lt;script"))
        assertTrue("Should contain escaped iframe", result.contains("&lt;iframe"))
        
        // Check that code blocks are preserved (but emojis in code are still replaced since we do emoji replacement first)
        assertTrue("Should preserve code block content", result.contains("println(\"😂\")"))
    }

    @Test
    fun `test markdown with only HTML sanitization needed`() {
        val input = "Normal text <script>evil()</script> more text"
        val result = MarkdownParser.parse(input)
        assertEquals("Normal text &lt;script>evil()&lt;/script&gt; more text", result)
    }

    @Test
    fun `test markdown with only emoji replacement needed`() {
        val input = "Hello :smile: and :heart: world"
        val result = MarkdownParser.parse(input)
        assertEquals("Hello 😄 and ❤️ world", result)
    }

    @Test
    fun `test order of operations - emoji then sanitization`() {
        // This test ensures emojis are replaced before HTML sanitization
        val input = ":smile:<script>test</script>:heart:"
        val result = MarkdownParser.parse(input)
        assertTrue("Should start with emoji", result.startsWith("😄"))
        assertTrue("Should end with emoji", result.endsWith("❤️"))
        assertTrue("Should contain escaped script", result.contains("&lt;script"))
    }
}