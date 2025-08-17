package com.chenge.markdown.core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * EmojiReplacer 单元测试
 */
class EmojiReplacerTest {

    @Test
    fun `test basic emoji replacement`() {
        val input = "Hello :smile: world :heart:"
        val expected = "Hello 😄 world ❤️"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test multiple same emojis`() {
        val input = ":smile: :smile: :smile:"
        val expected = "😄 😄 😄"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test unknown emoji shortcode`() {
        val input = "Hello :unknown_emoji: world"
        val expected = "Hello :unknown_emoji: world"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test empty string`() {
        val input = ""
        val expected = ""
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test string without emojis`() {
        val input = "This is a normal text without any emoji shortcodes."
        val expected = "This is a normal text without any emoji shortcodes."
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test partial emoji shortcode`() {
        val input = "Hello :smile world :heart"
        val expected = "Hello :smile world :heart"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test all supported emojis`() {
        val input = ":smile: :grin: :joy: :rofl: :wink: :blush: :sunglasses: :thinking:"
        val expected = "😄 😁 😂 🤣 😉 😊 😎 🤔"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test emoji in markdown context`() {
        val input = "# Hello :smile:\n\n**Bold text** with :heart: emoji\n\n- List item :thumbsup:"
        val expected = "# Hello 😄\n\n**Bold text** with ❤️ emoji\n\n- List item 👍"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test case sensitivity`() {
        val input = ":SMILE: :Smile: :smile:"
        val expected = ":SMILE: :Smile: 😄"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test nested colons`() {
        val input = "::smile:: :smile:"
        val expected = ":😄: 😄"
        val result = EmojiReplacer.replaceShortcodes(input)
        assertEquals(expected, result)
    }
}
