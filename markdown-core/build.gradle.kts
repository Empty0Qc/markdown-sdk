plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.core"
}

dependencies {
  implementation(project(":markdown-common"))
  
  // Test dependencies
  testImplementation(libs.junit)
}