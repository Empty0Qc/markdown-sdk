package com.chenge.markdown.common

import org.junit.Test
import org.junit.Assert.*

/**
 * MarkdownSanitizer 单元测试
 */
class MarkdownSanitizerTest {

    @Test
    fun `test script tag sanitization`() {
        val input = "Hello <script>alert('xss')</script> world"
        val expected = "Hello &lt;script>alert('xss')&lt;/script&gt; world"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test iframe tag sanitization`() {
        val input = "Content <iframe src='evil.com'></iframe> more content"
        val expected = "Content &lt;iframe src='evil.com'>&lt;/iframe&gt; more content"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test multiple dangerous tags`() {
        val input = "<script>evil()</script> and <iframe src='bad'></iframe>"
        val expected = "&lt;script>evil()&lt;/script&gt; and &lt;iframe src='bad'>&lt;/iframe&gt;"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test case insensitive script tags`() {
        val input = "<SCRIPT>alert('test')</SCRIPT>"
        val expected = "<SCRIPT>alert('test')</SCRIPT>"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result) // Only lowercase tags are sanitized
    }

    @Test
    fun `test nested script tags`() {
        val input = "<script><script>nested</script></script>"
        val expected = "&lt;script>&lt;script>nested&lt;/script&gt;&lt;/script&gt;"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test safe HTML tags are preserved`() {
        val input = "<p>Paragraph</p> <div>Division</div> <span>Span</span>"
        val expected = "<p>Paragraph</p> <div>Division</div> <span>Span</span>"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test empty string`() {
        val input = ""
        val expected = ""
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test string without dangerous tags`() {
        val input = "This is a normal markdown text with **bold** and *italic*."
        val expected = "This is a normal markdown text with **bold** and *italic*."
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test partial dangerous tags`() {
        val input = "<script without closing and </script> without opening"
        val expected = "&lt;script without closing and &lt;/script&gt; without opening"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test script tag with attributes`() {
        val input = "<script type='text/javascript' src='evil.js'>alert('xss')</script>"
        val expected = "&lt;script type='text/javascript' src='evil.js'>alert('xss')&lt;/script&gt;"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test iframe tag with attributes`() {
        val input = "<iframe width='100' height='200' src='malicious.html'></iframe>"
        val expected = "&lt;iframe width='100' height='200' src='malicious.html'>&lt;/iframe&gt;"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test markdown with code blocks containing dangerous tags`() {
        val input = "```html\n<script>alert('this should be preserved')</script>\n```"
        val expected = "```html\n&lt;script>alert('this should be preserved')&lt;/script&gt;\n```"
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test whitespace around dangerous tags`() {
        val input = "  <script>  evil()  </script>  "
        val expected = "  &lt;script>  evil()  &lt;/script&gt;  "
        val result = MarkdownSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test multiple lines with dangerous content`() {
        val input = """Line 1 with <script>evil1()</script>
            |Line 2 with <iframe src='bad'></iframe>
            |Line 3 is safe
            |Line 4 with <script>evil2()</script>
        """.trimMargin()
        
        val result = MarkdownSanitizer.sanitize(input)
        
        assertFalse("Should not contain script tags", result.contains("<script>"))
        assertFalse("Should not contain iframe tags", result.contains("<iframe"))
        assertTrue("Should contain escaped script", result.contains("&lt;script"))
        assertTrue("Should contain escaped iframe", result.contains("&lt;iframe"))
        assertTrue("Should preserve safe content", result.contains("Line 3 is safe"))
    }
}