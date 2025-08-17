# 📝 Markdown SDK for Android

一个高度模块化、可扩展、支持同步/异步渲染的 Android Markdown 渲染 SDK。基于 [Markwon](https://github.com/noties/Markwon) 构建，提供企业级的 Markdown 渲染解决方案。

---

## 🚀 项目亮点

<details>
<summary>展开查看项目亮点</summary>

✅ **模块化架构** - 6个独立模块，职责清晰，易于维护  
✅ **同步/异步渲染** - 支持高性能异步渲染，避免主线程阻塞  
✅ **丰富的扩展** - 支持表格、任务列表、LaTeX、删除线等  
✅ **自定义插件系统** - 灵活的插件架构，支持自定义扩展  
✅ **性能优化** - 专用线程池、对象池、缓存机制  
✅ **安全净化** - 内置 XSS 防护和内容安全过滤  
✅ **现代化 UI** - Material Design 3 样式支持  
✅ **调试工具** - 完整的性能分析和调试功能  
✅ **纯 Kotlin 实现** - 100% Kotlin，类型安全  
✅ **一键发布** - 自动化构建和发布脚本

</details>

---

## 📁 项目架构

<details>
<summary>展开查看项目架构</summary>

### 模块结构图

```
markdown-sdk/
├── app/                     # 示例应用
├── markdown-common/         # 公共组件和接口定义
├── markdown-core/           # 核心解析器和工具类
├── markdown-engine/         # 统一入口和引擎封装
├── markdown-render/         # 渲染逻辑和调度器
├── markdown-plugins/        # 插件系统和扩展
└── build-logic/            # 构建逻辑和发布脚本
```

### 模块依赖关系

```
app
 └── markdown-engine (统一入口)
     ├── markdown-core (核心解析)
     │   └── markdown-common (公共组件)
     ├── markdown-render (渲染逻辑)
     │   └── markdown-common
     └── markdown-plugins (插件系统)
         └── markdown-common
```

</details>

---

## 🏗️ 核心架构设计

<details>
<summary>展开查看核心架构设计</summary>

### 1. 分层架构

| 层级 | 模块 | 职责 |
|------|------|------|
| **应用层** | `app` | 示例应用，展示 SDK 使用方式 |
| **引擎层** | `markdown-engine` | 统一入口，封装所有功能 |
| **业务层** | `markdown-core`<br>`markdown-render`<br>`markdown-plugins` | 核心解析逻辑<br>渲染和调度<br>插件和扩展 |
| **基础层** | `markdown-common` | 公共接口、配置、工具类 |

### 2. 核心组件

#### MarkdownEngine - 统一入口
```kotlin
// 位置: markdown-engine/src/main/java/com/chenge/markdown/engine/MarkdownEngine.kt
class MarkdownEngine {
    companion object {
        fun with(context: Context): MarkdownEngine
        fun withPreset(context: Context, config: MarkdownConfig): MarkdownEngine
    }
    
    fun render(textView: TextView, markdown: String)
    fun renderAsync(textView: TextView, markdown: String, callback: (() -> Unit)?)
    fun config(config: MarkdownConfig): MarkdownEngine
    fun async(): MarkdownEngine
}
```

#### MarkdownRenderer - 渲染核心
```kotlin
// 位置: markdown-render/src/main/java/com/chenge/markdown/render/MarkdownRenderer.kt
object MarkdownRenderer {
    fun setMarkdownSync(markwon: Markwon, textView: TextView, markdown: String)
    fun setMarkdownAsync(markwon: Markwon, textView: TextView, markdown: String)
}
```

#### MarkdownScheduler - 异步调度
```kotlin
// 位置: markdown-render/src/main/java/com/chenge/markdown/render/MarkdownScheduler.kt
object MarkdownScheduler {
    fun asyncRender<T>(backgroundTask: () -> T, onResult: (T) -> Unit, onError: ((Throwable) -> Unit)?)
    fun executeRender(task: Runnable): Future<*>
    fun executeImage(task: Runnable): Future<*>
}
```

#### MarkdownPlugins - 插件系统
```kotlin
// 位置: markdown-plugins/src/main/java/com/chenge/markdown/MarkdownPlugins.kt
object MarkdownPlugins {
    fun register(plugin: MarkdownPlugin)
    fun create(context: Context, config: MarkdownConfig): Markwon
}
```

</details>

---

## 🧩 模块详细说明

<details>
<summary>展开查看模块详细说明</summary>

### markdown-common
**公共组件和接口定义模块**

**核心类:**
- `MarkdownConfig` - 全局配置类
- `MarkdownConfigBuilder` - 配置构建器 (DSL)
- `MarkdownPlugin` - 插件接口定义
- `MarkdownSanitizer` - 内容安全净化
- `MarkdownStyleConfigV2` - 现代化样式配置

**主要功能:**
- 提供统一的配置接口
- 定义插件系统规范
- 实现内容安全过滤
- 支持 Material Design 3 样式

### markdown-core
**核心解析器和工具类模块**

**核心类:**
- `MarkdownParser` - 基础解析器
- `EmojiReplacer` - Emoji 短代码替换
- `MarkdownLoader` - Assets 文件加载器

**主要功能:**
- Markdown 内容预处理
- Emoji 短代码转换
- 从 Assets 加载 Markdown 文件
- 内容净化和安全过滤

### markdown-render
**渲染逻辑和调度器模块**

**核心类:**
- `MarkdownRenderer` - 渲染器
- `MarkdownScheduler` - 异步调度器
- `MarkdownView` - 自定义 TextView

**主要功能:**
- 同步/异步渲染支持
- 专用线程池管理
- 性能监控和统计
- 自定义渲染控件

**线程池设计:**
- 渲染线程池: 2-4 个线程，专用于 Markdown 解析
- 图片线程池: 4 个线程，专用于图片加载
- 主线程调度: Handler 机制确保 UI 更新

### markdown-plugins
**插件系统和扩展模块**

**核心插件:**
- `ClickablePlugin` - 链接和代码点击处理
- `ImageSizePlugin` - 图片尺寸解析
- `StylePlugin` - 样式插件
- `ModernMarkdownEngine` - 现代化引擎

**扩展插件 (基于 Markwon):**
- `TablePlugin` - 表格支持
- `TaskListPlugin` - 任务列表
- `StrikethroughPlugin` - 删除线
- `JLatexMathPlugin` - LaTeX 数学公式
- `GlideImagesPlugin` - 图片加载 (Glide)

**插件架构:**
```kotlin
interface MarkdownPlugin {
    val name: String
    val version: String
    val enabled: Boolean
    fun configure(config: Map<String, Any>)
}
```

### markdown-engine
**统一入口和引擎封装模块**

**核心类:**
- `MarkdownEngine` - 主入口类
- `MarkdownConfigExamples` - 配置示例
- `PluginsBridge` - 插件桥接 (typealias)

**主要功能:**
- 提供统一的 API 入口
- 封装所有底层模块
- 简化使用方式
- 向后兼容性保证

### app
**示例应用模块**

**主要功能:**
- 展示 SDK 各种使用方式
- 性能测试和基准测试
- 同步/异步渲染对比
- 插件功能演示

</details>

---

## 🛠️ 快速开始

<details>
<summary>展开查看快速开始</summary>

### 1. 添加依赖

在 `settings.gradle.kts` 中:
```kotlin
include(":markdown-engine")
```

在 `build.gradle.kts` 中:
```kotlin
dependencies {
    implementation(project(":markdown-engine"))
}
```

### 2. 基本使用

```kotlin
// 创建引擎实例
val engine = MarkdownEngine.with(context)

// 渲染 Markdown
engine.render(textView, markdownString)
```

### 3. 异步渲染

```kotlin
// 异步渲染，避免主线程阻塞
val engine = MarkdownEngine.with(context).async()
engine.render(textView, markdownString)
```

### 4. 自定义配置

```kotlin
// 使用 DSL 配置
val engine = MarkdownEngine.with(context) {
    enableAll()           // 启用所有功能
    async()              // 异步渲染
    debug()              // 调试模式
    imageSize(1200, 800) // 图片尺寸限制
}
```

### 5. 预设配置

```kotlin
// 博客模式 - 支持完整功能
val blogEngine = MarkdownEngine.withPreset(context, MarkdownConfig.blog())

// 聊天模式 - 轻量级，快速渲染
val chatEngine = MarkdownEngine.withPreset(context, MarkdownConfig.chat())

// 编辑器模式 - 支持实时预览
val editorEngine = MarkdownEngine.withPreset(context, MarkdownConfig.editor())
```

</details>

---

## 🔧 高级功能

<details>
<summary>展开查看高级功能</summary>

### 自定义插件开发

```kotlin
class CustomPlugin : MarkdownPlugin {
    override val name = "CustomPlugin"
    override val version = "1.0.0"
    override val enabled = true
    
    override fun configure(config: Map<String, Any>) {
        // 插件配置逻辑
    }
    
    override fun apply(builder: Markwon.Builder) {
        // 插件应用逻辑
    }
}

// 注册插件
MarkdownPlugins.register(CustomPlugin())
```

### 性能监控

```kotlin
// 获取性能统计
val stats = engine.getPerformanceStats()
println("渲染耗时: ${stats.renderTime}ms")
println("内存使用: ${stats.memoryUsage}MB")
```

### 从 Assets 加载

```kotlin
// 从 assets 加载 Markdown 文件
val markdown = MarkdownLoader.loadFromAssets(context, "sample.md")
engine.render(textView, markdown)
```

</details>

---

## 📊 性能特性

<details>
<summary>展开查看性能特性</summary>

### 异步渲染架构
- **渲染线程池**: 2-4 个专用线程，避免主线程阻塞
- **图片加载线程池**: 4 个线程，并行加载图片资源
- **智能调度**: 根据内容复杂度动态调整渲染策略

### 缓存机制
- **Markwon 实例池**: 复用 Markwon 实例，减少创建开销
- **TextView 对象池**: 复用 TextView，优化内存使用
- **图片缓存**: 基于 Glide 的智能图片缓存

### 内存优化
- **弱引用管理**: 避免内存泄漏
- **及时回收**: 主动释放不需要的资源
- **内存监控**: 实时监控内存使用情况

</details>

---

## 🔒 安全特性

<details>
<summary>展开查看安全特性</summary>

### XSS 防护
```kotlin
// 自动过滤危险标签
MarkdownSanitizer.sanitize(input)
// 过滤: <script>, <iframe>, <object> 等
```

### 内容净化
- 移除潜在的恶意 HTML 标签
- 过滤危险的 JavaScript 代码
- 限制外部资源加载

</details>

---

## 🎨 样式定制

<details>
<summary>展开查看样式定制</summary>

### Material Design 3 支持
```kotlin
val styleConfig = MarkdownStyleConfigV2 {
    primaryColor = Color.parseColor("#6750A4")
    surfaceColor = Color.parseColor("#FEF7FF")
    textColor = Color.parseColor("#1C1B1F")
    codeBackgroundColor = Color.parseColor("#F3F3F3")
}
```

### 自定义主题
- 支持亮色/暗色主题
- 可自定义字体、颜色、间距
- 响应式设计，适配不同屏幕尺寸

</details>

---

## 🧪 测试和调试

<details>
<summary>展开查看测试和调试</summary>

### 调试模式
```kotlin
val engine = MarkdownEngine.with(context) {
    debug() // 启用调试模式
}
```

### 性能分析
- 渲染耗时统计
- 内存使用监控
- 线程池状态查看
- 缓存命中率分析

</details>

---

## 📦 构建和发布

<details>
<summary>展开查看构建和发布</summary>

### 本地发布
```bash
./publish.sh
```

### 版本管理
- 语义化版本控制 (SemVer)
- 自动化构建流程
- 依赖版本锁定

</details>

---

## 🤝 贡献指南

<details>
<summary>展开查看贡献指南</summary>

### 开发环境
- Android Studio 2023.1+
- Kotlin 1.9.22+
- Gradle 8.6+
- JDK 17+

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 ktlint 进行代码格式化
- 编写单元测试和集成测试

### 提交规范
- 使用 Conventional Commits 规范
- 提供清晰的 commit message
- 包含相关的测试用例

</details>

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 🙏 致谢

- [Markwon](https://github.com/noties/Markwon) - 优秀的 Android Markdown 渲染库
- [Glide](https://github.com/bumptech/glide) - 强大的图片加载库
- [Material Design](https://material.io/) - Google 的设计系统

---

## 📞 支持

如需帮助或问题反馈，请:
- 提交 [Issue](https://github.com/your-username/markdown-sdk/issues)
- 查看 [Wiki](https://github.com/your-username/markdown-sdk/wiki)
- 联系维护团队

---

**让 Markdown 渲染变得简单而强大！** 🚀