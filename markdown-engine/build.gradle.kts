plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.engine"
}

dependencies {
  // 依赖核心模块 - 使用 api 暴露给上层（为 typealias 提供字节码）
  api(project(":markdown-core"))
  // 依赖渲染和插件模块 - 使用 api 暴露给上层
  api(project(":markdown-render"))
  api(project(":markdown-plugins"))
  api(project(":markdown-common"))
}