plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.common"
}

dependencies {
  // Test dependencies
  testImplementation(libs.junit)
}
