plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.render"
}

dependencies {
  implementation(project(":markdown-common"))
  implementation(project(":markdown-core"))
  implementation(project(":markdown-plugins"))
  implementation(libs.markwon.core)
  implementation(libs.androidx.appcompat)
  implementation(libs.kotlin.coroutines.android)
  
  testImplementation(libs.junit)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.test.espresso.core)
}