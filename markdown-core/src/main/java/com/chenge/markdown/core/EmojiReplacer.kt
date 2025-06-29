package com.chenge.markdown.core

/**
 * Emoji 替换工具类
 *
 * 用法：
 * val processedText = EmojiReplacer.replaceShortcodes(rawMarkdown)
 */
object EmojiReplacer {

  /**
   * Emoji 映射表
   * 可以根据需要扩充
   */
  private val emojiMap = mapOf(
    ":smile:" to "\uD83D\uDE04",       // 😄
    ":grin:" to "\uD83D\uDE01",        // 😁
    ":joy:" to "\uD83D\uDE02",         // 😂
    ":rofl:" to "\uD83E\uDD23",        // 🤣
    ":wink:" to "\uD83D\uDE09",        // 😉
    ":blush:" to "\uD83D\uDE0A",       // 😊
    ":sunglasses:" to "\uD83D\uDE0E",  // 😎
    ":thinking:" to "\uD83E\uDD14",    // 🤔
    ":neutral:" to "\uD83D\uDE10",     // 😐
    ":expressionless:" to "\uD83D\uDE11", // 😑
    ":sleeping:" to "\uD83D\uDE34",    // 😴
    ":cry:" to "\uD83D\uDE22",         // 😢
    ":sob:" to "\uD83D\uDE2D",         // 😭
    ":angry:" to "\uD83D\uDE20",       // 😠
    ":rage:" to "\uD83D\uDE21",        // 😡
    ":thumbsup:" to "\uD83D\uDC4D",    // 👍
    ":thumbsdown:" to "\uD83D\uDC4E",  // 👎
    ":clap:" to "\uD83D\uDC4F",        // 👏
    ":ok_hand:" to "\uD83D\uDC4C",     // 👌
    ":pray:" to "\uD83D\uDE4F",        // 🙏
    ":muscle:" to "\uD83D\uDCAA",      // 💪
    ":wave:" to "\uD83D\uDC4B",        // 👋
    ":heart:" to "\u2764\uFE0F",       // ❤️
    ":blue_heart:" to "\uD83D\uDC99",  // 💙
    ":green_heart:" to "\uD83D\uDC9A", // 💚
    ":purple_heart:" to "\uD83D\uDC9C",// 💜
    ":yellow_heart:" to "\uD83D\uDC9B",// 💛
    ":broken_heart:" to "\uD83D\uDC94",// 💔
    ":star:" to "\u2B50",              // ⭐
    ":fire:" to "\uD83D\uDD25",        // 🔥
    ":sparkles:" to "\u2728",          // ✨
    ":tada:" to "\uD83C\uDF89",        // 🎉
    ":balloon:" to "\uD83C\uDF88",     // 🎈
    ":gift:" to "\uD83C\uDF81",        // 🎁
    ":rocket:" to "\uD83D\uDE80",      // 🚀
    ":100:" to "\uD83D\uDCAF",         // 💯
    ":check:" to "\u2714\uFE0F",       // ✔️
    ":x:" to "\u274C",                 // ❌
    ":warning:" to "\u26A0\uFE0F",     // ⚠️
    ":zap:" to "\u26A1",               // ⚡
    ":sun:" to "\u2600\uFE0F",         // ☀️
    ":moon:" to "\uD83C\uDF19",        // 🌙
    ":cloud:" to "\u2601\uFE0F",       // ☁️
    ":rainbow:" to "\uD83C\uDF08",     // 🌈
    ":snowflake:" to "\u2744\uFE0F",   // ❄️
    ":star2:" to "\uD83C\uDF1F",       // 🌟
    ":crown:" to "\uD83D\uDC51",       // 👑
    ":moneybag:" to "\uD83D\uDCB0",    // 💰
    ":money_with_wings:" to "\uD83D\uDCB8", // 💸
    ":chart_up:" to "\uD83D\uDCC8",    // 📈
    ":chart_down:" to "\uD83D\uDCC9",  // 📉
    ":eyes:" to "\uD83D\uDC40",        // 👀
    ":see_no_evil:" to "\uD83D\uDE48", // 🙈
    ":poop:" to "\uD83D\uDCA9",        // 💩
    ":skull:" to "\uD83D\uDC80",       // 💀
    ":robot:" to "\uD83E\uDD16",       // 🤖
    ":alien:" to "\uD83D\uDC7D",       // 👽
    ":sparkling_heart:" to "\uD83D\uDC96", // 💖
    ":boom:" to "\uD83D\uDCA5",        // 💥
    ":collision:" to "\uD83D\uDCA5",   // 💥
    ":dizzy:" to "\uD83C\uDF1F",       // 🌟
  )


  /**
   * 替换所有短代码为 Emoji
   *
   * @param input 原始文本
   * @return 替换后的文本
   */
  fun replaceShortcodes(input: String): String {
    if (input.isEmpty()) return input

    var result = input
    emojiMap.forEach { (shortcode, emoji) ->
      result = result.replace(shortcode, emoji)
    }
    return result
  }
}
