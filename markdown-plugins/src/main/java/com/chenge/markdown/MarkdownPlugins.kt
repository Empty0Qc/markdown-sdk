package com.chenge.markdown.plugins

import android.content.Context
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tables.TablePlugin
import com.chenge.markdown.common.MarkdownConfig


/**
 * 工厂方法：根据配置创建Markwon
 */
object MarkdownPlugins {

  fun create(context: Context, config: MarkdownConfig): Markwon {
    val builder = Markwon.builder(context)

    if (config.enableTables) {
      builder.usePlugin(TablePlugin.create(context))
    }

    if (config.enableTaskList) {
      builder.usePlugin(TaskListPlugin.create(context))
    }

    // 如需HTML支持，未来可用HtmlPlugin

    return builder.build()
  }
}
