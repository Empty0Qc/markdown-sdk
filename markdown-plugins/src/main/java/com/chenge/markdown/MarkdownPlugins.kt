package com.chenge.markdown.plugins

import android.content.Context
import com.chenge.markdown.common.MarkdownConfig
import com.chenge.markdown.common.CodeHighlight
import com.chenge.markdown.plugins.MarkdownPlugins.create
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.prism4j.themes.Prism4jThemeDarkula
import io.noties.prism4j.themes.Prism4jThemeDefault
import io.noties.prism4j.GrammarLocatorDefault

/**
 * 插件注册与 Markwon 创建器
 */
object MarkdownPlugins {

  /** 所有注册的插件 */
  private val pluginList = mutableListOf<MarkdownPlugin>()

  /**
   * 注册自定义插件，在调用 [create] 之前执行
   */
  fun register(plugin: MarkdownPlugin) {
    pluginList += plugin
  }

  /**
   * 根据配置和已注册插件构造 [Markwon]
   */
  fun create(context: Context, config: MarkdownConfig): Markwon {
    val builder = Markwon.builder(context).usePlugin(CorePlugin.create())
      .usePlugin(MarkwonInlineParserPlugin.create()).usePlugin(
        JLatexMathPlugin.create(32F, { builder ->
          builder.inlinesEnabled(true)
        })
      ).usePlugin(StrikethroughPlugin.create()).usePlugin(GlideImagesPlugin.create(context))


    pluginList.forEach { it.apply(builder) }

    if (config.enableTables) {
      builder.usePlugin(TablePlugin.create(context))
    }

    if (config.enableTaskList) {
      builder.usePlugin(TaskListPlugin.create(context))
    }

    when (config.codeHighlight) {
      CodeHighlight.SIMPLE -> {
        builder.usePlugin(
          SyntaxHighlightPlugin.create(SimpleSyntaxHighlight())
        )
      }
      CodeHighlight.PRISM_LIGHT -> {
        val prism4j = Prism4j(GrammarLocatorDefault())
        builder.usePlugin(
          SyntaxHighlightPlugin.create(
            Prism4jSyntaxHighlight.create(prism4j, Prism4jThemeDefault.create())
          )
        )
      }
      CodeHighlight.PRISM_DARK -> {
        val prism4j = Prism4j(GrammarLocatorDefault())
        builder.usePlugin(
          SyntaxHighlightPlugin.create(
            Prism4jSyntaxHighlight.create(prism4j, Prism4jThemeDarkula.create())
          )
        )
      }
      else -> {}
    }

    // 如需HTML支持，未来可用HtmlPlugin
    if (config.enableHtml) {
      builder.usePlugin(HtmlPlugin.create { plugin ->
      })
    }
    return builder.build()
  }
}
