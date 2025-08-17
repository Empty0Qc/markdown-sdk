plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.plugins"
}

dependencies {
  implementation(project(":markdown-common"))
  api(libs.bundles.markwon.core.bundle)
  api(libs.bundles.markwon.extensions)
  api(libs.bundles.markwon.image.bundle)
  
  // AndroidX dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.bundles.androidx.lifecycle)
  
  // Kotlin Coroutines
  implementation(libs.kotlin.coroutines.core)
  implementation(libs.kotlin.coroutines.android)
}