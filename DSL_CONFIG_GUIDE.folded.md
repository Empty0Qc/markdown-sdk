# MarkdownConfig DSL 配置指南

本指南介绍如何使用新的 DSL 配置功能来配置 MarkdownSDK。

## 概述

<details>
<summary>展开查看配置方式概述</summary>

MarkdownSDK 现在提供了三种配置方式：

1. **传统配置方式** - 直接使用 `MarkdownConfig` 构造函数
2. **预设配置** - 使用内置的配置预设
3. **DSL 配置** - 使用 Kotlin DSL 语法（推荐）

</details>

## 1. 传统配置方式

```kotlin
val config = MarkdownConfig(
    enableHtml = true,
    enableTables = true,
    enableTaskList = true,
    enableLatex = false,
    enableImageLoading = true,
    maxImageWidth = 800,
    maxImageHeight = 600
)

val engine = MarkdownEngine.with(context).config(config)
```

## 2. 预设配置

### 可用预设

<details>
<summary>展开查看可用预设列表</summary>

- `MarkdownConfig.default()` - 默认配置
- `MarkdownConfig.blog()` - 博客/文档配置（支持表格、任务列表、LaTeX）
- `MarkdownConfig.chat()` - 聊天/消息配置（轻量级，快速渲染）
- `MarkdownConfig.editor()` - 富文本编辑器配置（支持所有功能）
- `MarkdownConfig.safe()` - 安全模式配置（禁用 HTML）
- `MarkdownConfig.performance()` - 性能优化配置
- `MarkdownConfig.full()` - 完整功能配置

</details>

### 使用示例

```kotlin
// 博客配置
val blogEngine = MarkdownEngine.withPreset(context, MarkdownConfig.blog())

// 聊天配置
val chatEngine = MarkdownEngine.withPreset(context, MarkdownConfig.chat())

// 编辑器配置
val editorEngine = MarkdownEngine.withPreset(context, MarkdownConfig.editor())
```

## 3. DSL 配置（推荐）

### 基本用法

```kotlin
val engine = MarkdownEngine.with(context) {
    tables()        // 启用表格支持
    taskLists()     // 启用任务列表支持
    latex()         // 启用 LaTeX 支持
    async()         // 启用异步渲染
    debug()         // 启用调试模式
    imageSize(800, 600)  // 设置图片尺寸
}
```

### 可用的 DSL 方法

<details>
<summary>展开查看 DSL 方法详情</summary>

#### 功能开关
- `tables()` - 启用表格支持
- `taskLists()` - 启用任务列表支持
- `latex()` - 启用 LaTeX 支持
- `async()` - 启用异步渲染
- `debug()` - 启用调试模式
- `safeMode()` - 启用安全模式（禁用 HTML）

#### 批量操作
- `enableAll()` - 启用所有功能
- `disableAll()` - 禁用所有功能（最小配置）

#### 配置设置
- `imageSize(width: Int, height: Int)` - 设置图片最大尺寸
- `plugin(name: String)` - 添加自定义插件

#### 直接属性设置
```kotlin
val engine = MarkdownEngine.with(context) {
    enableHtml = true
    enableImageLoading = false
    maxImageWidth = 1200
    maxImageHeight = 800
}
```

</details>

### 高级用法

#### 条件配置

```kotlin
val engine = MarkdownEngine.with(context) {
    tables()
    taskLists()
    
    if (BuildConfig.DEBUG) {
        debug()
    }
    
    if (isAdvancedUser) {
        latex()
        enableHtml = true
    } else {
        safeMode()
    }
    
    async()
    imageSize(800, 600)
}
```

#### 自定义插件配置

```kotlin
val engine = MarkdownEngine.with(context) {
    enableAll()
    plugin("syntax-highlight")
    plugin("math-formula")
    plugin("custom-emoji")
}
```

#### 性能优化配置

```kotlin
val performanceEngine = MarkdownEngine.with(context) {
    disableAll()
    enableImageLoading = true
    enableLinkClick = true
    async()
}
```

### 独立 DSL 配置

<details>
<summary>展开查看独立配置示例</summary>

你也可以创建独立的配置对象：

```kotlin
val config = markdownConfig {
    tables()
    taskLists()
    latex()
    imageSize(1024, 768)
    async()
}

val engine = MarkdownEngine.withPreset(context, config)
```

</details>

## 配置对比

<details>
<summary>展开查看配置对比表格</summary>

| 功能 | 默认 | 博客 | 聊天 | 编辑器 | 安全 | 性能 |
|------|------|------|------|--------|------|------|
| HTML | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| 表格 | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ |
| 任务列表 | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ |
| LaTeX | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| 图片加载 | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 链接点击 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 异步渲染 | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ |
| 调试模式 | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |

</details>

## 最佳实践

<details>
<summary>展开查看最佳实践建议</summary>

1. **使用 DSL 配置** - 更简洁、更易读
2. **选择合适的预设** - 根据使用场景选择预设配置
3. **启用异步渲染** - 对于大文档或性能敏感的场景
4. **安全第一** - 对于用户生成内容，使用 `safeMode()`
5. **调试模式** - 开发阶段启用 `debug()` 获取更多信息

</details>

## 向后兼容性

所有现有的配置方式仍然支持，新的 DSL 配置是额外的便利功能，不会破坏现有代码。