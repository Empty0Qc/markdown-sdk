package com.chenge.markdown.render

import android.content.Context
import android.text.Spanned
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatTextView
import com.chenge.markdown.common.MarkdownStyleConfigV2
import com.chenge.markdown.plugins.CustomViewRenderer
import io.noties.markwon.Markwon

/**
 * MarkdownView：封装Markwon渲染，支持自定义视图
 */
class MarkdownView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

  private val containerLayout: LinearLayout
  private val textView: AppCompatTextView
  private val customViewRenderer: CustomViewRenderer
  private var config: MarkdownStyleConfigV2 = MarkdownStyleConfigV2()
  
  private var markwon: Markwon

  init {
    // 创建容器布局
    containerLayout = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
    }
    
    // 创建文本视图
    textView = AppCompatTextView(context).apply {
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      )
    }
    
    // 创建自定义视图渲染器
    customViewRenderer = CustomViewRenderer(context)
    customViewRenderer.setStyleConfig(config)
    
    // 初始化Markwon
    markwon = createMarkwon()
    
    containerLayout.addView(textView)
    addView(containerLayout)
  }
  
  /**
   * 设置样式配置
   */
  fun setStyleConfig(config: MarkdownStyleConfigV2) {
    this.config = config
    customViewRenderer.setStyleConfig(config)
    markwon = createMarkwon()
  }
  
  /**
   * 创建Markwon实例
   */
  private fun createMarkwon(): Markwon {
    return Markwon.builder(context)
      .usePlugin(customViewRenderer.createCustomViewPlugin())
      .build()
  }

  /**
   * 设置Markdown内容
   */
  fun setMarkdown(markdown: String) {
    // 清除之前的自定义视图
    clearCustomViews()
    
    // 解析并添加自定义视图
    addCustomViews(markdown)
    
    // 移除已处理的表格和引用块内容，避免重复显示
    val processedMarkdown = removeProcessedContent(markdown)
    
    // 渲染文本内容
    markwon.setMarkdown(textView, processedMarkdown)
  }
  
  /**
   * 设置Spanned内容
   */
  fun setMarkdown(spanned: Spanned) {
    clearCustomViews()
    textView.text = spanned
  }
  
  /**
   * 清除自定义视图
   */
  private fun clearCustomViews() {
    // 移除除了textView之外的所有视图
    val viewsToRemove = mutableListOf<android.view.View>()
    for (i in 0 until containerLayout.childCount) {
      val child = containerLayout.getChildAt(i)
      if (child != textView) {
        viewsToRemove.add(child)
      }
    }
    viewsToRemove.forEach { containerLayout.removeView(it) }
  }
  
  /**
   * 添加自定义视图
   */
  private fun addCustomViews(markdown: String) {
    // 解析表格
    val tables = customViewRenderer.parseTableFromMarkdown(markdown)
    tables.forEach { tableData ->
      val tableView = customViewRenderer.renderTableToView(tableData)
      tableView.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        setMargins(0, config.tableCornerRadius.toInt(), 0, config.tableCornerRadius.toInt())
      }
      containerLayout.addView(tableView)
    }
    
    // 解析引用块
    val quotes = customViewRenderer.parseQuotesFromMarkdown(markdown)
    quotes.forEach { quoteData ->
      val quoteView = customViewRenderer.renderQuoteToView(quoteData)
      quoteView.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        setMargins(0, config.blockquoteMargin, 0, config.blockquoteMargin)
      }
      containerLayout.addView(quoteView)
    }
  }
  
  /**
   * 移除已处理的表格和引用块内容
   */
  private fun removeProcessedContent(markdown: String): String {
    var processedMarkdown = markdown
    
    // 移除表格内容
    val lines = processedMarkdown.split("\n").toMutableList()
    var i = 0
    
    while (i < lines.size) {
      val line = lines[i].trim()
      val cleanLine = line.removePrefix(">").trim()
      
      // 检查是否是表格行
      if (cleanLine.startsWith("|") && cleanLine.endsWith("|")) {
        // 找到表格开始，移除整个表格
        val tableStartIndex = i
        var j = i
        
        // 找到表格结束
        while (j < lines.size) {
          val currentLine = lines[j].trim()
          val currentCleanLine = currentLine.removePrefix(">").trim()
          
          if (currentCleanLine.startsWith("|") && currentCleanLine.endsWith("|")) {
            j++
          } else {
            break
          }
        }
        
        // 移除表格行
        for (k in tableStartIndex until j) {
          lines.removeAt(tableStartIndex)
        }
        
        i = tableStartIndex
      } else {
        i++
      }
    }
    
    return lines.joinToString("\n")
  }
  
  /**
   * 获取文本视图（用于兼容性）
   */
  fun getTextView(): AppCompatTextView = textView
  
  /**
   * 获取容器布局
   */
  fun getContainerLayout(): LinearLayout = containerLayout
  
  /**
   * 设置文本大小（委托给内部TextView）
   */
  fun setTextSize(unit: Int, size: Float) {
    textView.setTextSize(unit, size)
  }
  
  /**
   * 设置文本颜色（委托给内部TextView）
   */
  fun setTextColor(color: Int) {
    textView.setTextColor(color)
  }
  
  /**
   * 设置背景颜色（委托给内部TextView）
   */
  override fun setBackgroundColor(color: Int) {
    textView.setBackgroundColor(color)
  }
  
  /**
   * 获取文本内容（委托给内部TextView）
   */
  fun getText(): CharSequence = textView.text
}
