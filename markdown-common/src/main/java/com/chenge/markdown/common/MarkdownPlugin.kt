package com.chenge.markdown.common

/**
 * 插件接口 - 定义稳定的插件 API
 * 用于解耦核心逻辑与具体插件实现
 */
interface MarkdownPlugin {
    /**
     * 插件名称
     */
    val name: String
    
    /**
     * 插件版本
     */
    val version: String
    
    /**
     * 插件是否启用
     */
    val enabled: Boolean
    
    /**
     * 配置插件
     * @param config 插件配置参数
     */
    fun configure(config: Map<String, Any> = emptyMap())
}

/**
 * 图片插件接口
 */
interface ImagePlugin : MarkdownPlugin {
    /**
     * 处理图片尺寸
     * @param imageUrl 图片 URL
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 处理后的尺寸信息
     */
    fun processImageSize(imageUrl: String, maxWidth: Int, maxHeight: Int): ImageSize
}

/**
 * 点击事件插件接口
 */
interface ClickablePlugin : MarkdownPlugin {
    /**
     * 处理链接点击
     * @param url 链接 URL
     * @return 是否已处理该点击事件
     */
    fun handleLinkClick(url: String): Boolean
}

/**
 * 图片尺寸数据类
 */
data class ImageSize(
    val width: Int,
    val height: Int,
    val aspectRatio: Float = width.toFloat() / height.toFloat()
)

/**
 * 插件注册器接口
 */
interface PluginRegistry {
    /**
     * 注册插件
     * @param plugin 要注册的插件
     */
    fun register(plugin: MarkdownPlugin)
    
    /**
     * 注销插件
     * @param pluginName 插件名称
     */
    fun unregister(pluginName: String)
    
    /**
     * 获取所有已注册的插件
     * @return 插件列表
     */
    fun getPlugins(): List<MarkdownPlugin>
    
    /**
     * 根据类型获取插件
     * @param pluginType 插件类型
     * @return 匹配的插件列表
     */
    fun <T : MarkdownPlugin> getPluginsByType(pluginType: Class<T>): List<T>
}