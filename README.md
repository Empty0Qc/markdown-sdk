# 📝 Markdown SDK for Android

一个高度模块化、可扩展、支持同步/异步渲染的 Android Markdown 渲染 SDK。

---

## 🚀 项目亮点

✅ 支持同步、异步渲染
✅ 支持表格、任务列表扩展
✅ 提供调试渲染能力（含耗时分析）
✅ 纯 Kotlin 实现
✅ 多模块分层架构
✅ 一键发布脚本
✅ 高度可定制配置
✅ Emoji 短代码替换与内容净化
✅ 支持流式增量渲染
✅ 提供 MarkdownView 控件及 Assets 加载

---

## 📁 模块结构

```
markdown-sdk/
├── markdown-core        // 核心引擎（MarkdownEngine）
├── markdown-render      // 渲染逻辑封装
├── markdown-plugins     // Markwon 插件封装
├── markdown-debug       // 调试渲染工具
├── markdown-sample      // 示例 App
```

---

## ✨ 功能能力

| 模块               | 功能说明                                    |
| ---------------- | --------------------------------------- |
| markdown-core    | 核心入口 `MarkdownEngine`，包含 Emoji 解析、内容净化与配置管理 |
| markdown-render  | 封装 Markwon，提供同步/异步及流式渲染，并包含 `MarkdownView` |
| markdown-plugins | 提供 `TablePlugin`、`TaskListPlugin` 等扩展，并支持自定义插件 |
| markdown-debug   | 调试渲染（含耗时打印），支持多种调试模式 |
| markdown-sample  | 示例项目，包含渲染演示与性能测试 |

---

## 🛠️ 安装使用

### 1⃣ 添加依赖

> 如果你要在其他项目中使用，需先通过 `./publish.sh` 发布到本地 maven 仓库。

在主工程 `settings.gradle.kts` 中添加模块引用：

```kotlin
include(":markdown-core", ":markdown-render", ":markdown-plugins", ":markdown-debug")
```

在 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation(project(":markdown-core"))
}
```

---

### 2⃣ 初始化 MarkdownEngine

```kotlin
val engine = MarkdownEngine.with(context)
```

**切换异步渲染：**

```kotlin
val engine = MarkdownEngine.with(context).async()
```

**自定义配置：**

```kotlin
val engine = MarkdownEngine.with(context).config(
    MarkdownConfig(enableTables = true, enableTaskList = true)
)
```

---

### 3⃣ 渲染 Markdown

```kotlin
engine.render(textView, markdownString)
```

---

## 🧬 调试与性能测试

**MarkdownDebugRenderer** 提供耗时分析和多模式渲染：

```kotlin
val debugRenderer = MarkdownDebugRenderer(context)
debugRenderer.render(textView, markdown, async = true)
```

在 Sample 中，悬浮按钮可直接触发：

* **渲染并打印耗时**
* **切换同步/异步模式**

---

## ⚙️ 一键发布脚本

在根目录执行：

```bash
./publish.sh
```

该脚本会：

✅ 自动递增版本
✅ 自动打 Git Tag
✅ 执行清理和发布
✅ 回显版本号和发布状态

如需禁用 Git Tag，可修改 `publish.sh` 中 git 部分。

---

## 🚀 性能对比示例

在 `MainActivity` 中，通过如下方式对比同步和异步性能：

```kotlin
val start = System.currentTimeMillis()
MarkdownRenderer.setMarkdownSync(markwon, textView, markdown)
val duration = System.currentTimeMillis() - start
Log.d("MarkdownSync", "同步耗时: $duration ms")
```

异步：

```kotlin
val start = System.currentTimeMillis()
MarkdownRenderer.setMarkdownAsync(markwon, textView, markdown) {
    val duration = System.currentTimeMillis() - start
    Log.d("MarkdownAsync", "异步耗时: $duration ms")
}
```

---

## 🎯 核心类

| 类名                    | 功能              |
| --------------------- | --------------- |
| MarkdownEngine        | SDK 核心入口        |
| MarkdownRenderer      | 渲染封装（同步/异步）     |
| MarkdownDebugRenderer | 调试渲染，耗时分析       |
| MarkdownPlugins       | Markwon 插件创建器   |
| MarkdownConfig        | 渲染配置参数类         |
| MarkdownSanitizer     | Markdown 内容安全过滤 |
| MarkdownLoader        | 从 assets 加载 Markdown 文件 |
| MarkdownParser        | 解析并净化 Markdown 内容 |
| EmojiReplacer         | Emoji 短代码替换 |
| MarkdownView          | 自定义 TextView 渲染控件 |

| MarkdownPlugin        | 插件统一接口 |
| ClickablePlugin       | 链接/代码点击回调 |
| ImageSizePlugin       | 按 URL 参数解析图片尺寸 |
| StylePlugin           | 样式插件，依赖 MarkdownStyleConfig |
| MarkdownStyleConfig   | 样式配置参数 |

---

## 🍿 发布版本规范

版本号格式：`major.minor.patch`

示例：`1.0.0`

---

## 📋 更新日志

### v1.0.1 (最新)

#### 🐛 问题修复

✅ **修复主页面底部按钮显示问题**
- 解决了底部按钮只有按下才能看到部分内容的显示问题
- 优化了按钮布局和可见性

✅ **优化 Markdown 内容触摸反馈**
- 为 Markdown 内容区域添加了 Material Design 风格的水波按压效果
- 在 `ComposeActivity.kt` 和 `activity_main.xml` 中启用了 `selectableItemBackground`
- 提升了用户交互体验，保持滚动功能正常

✅ **修复 Compose 页面滚动问题**
- 解决了 Compose 页面渲染结果无法滚动查看下面内容的问题
- 移除了 `MarkdownView` 的 `heightIn` 最大高度限制（`max = 600.dp`）
- 确保 Markdown 内容可以完整显示和正常滚动

✅ **修复 ScrollBarDrawable 空指针崩溃**
- 解决了 Compose 页面中 ScrollBarDrawable 相关的空指针异常
- 提升了应用稳定性

#### 🔧 技术改进

- 优化了 `AndroidView` 组件的配置
- 改进了触摸事件处理机制
- 增强了 Compose 与传统 View 的兼容性

---

## 🙏 致谢

基于 [Markwon](https://github.com/noties/Markwon) 构建。

---

如需更多帮助或问题反馈，请提交 [Issue](https://github.com/your-username/markdown-sdk/issues)。
