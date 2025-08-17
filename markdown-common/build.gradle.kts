plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.common"
}

dependencies {
  // Android annotations
  implementation("androidx.annotation:annotation:1.7.1")
  
  // Test dependencies
  testImplementation(libs.junit)
}
