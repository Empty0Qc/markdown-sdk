plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.render"
}

dependencies {
  implementation(project(":markdown-common"))
  implementation(libs.markwon.core)
  implementation(libs.androidx.appcompat)
}