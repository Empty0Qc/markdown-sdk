plugins {
  alias(libs.plugins.markdownsdk.android.library)
  alias(libs.plugins.markdownsdk.publishing)
}

android {
  namespace = "com.chenge.markdown.debug"
}

dependencies {
  implementation(project(":markdown-engine"))
  implementation(libs.markwon.core)
  implementation(libs.markwon.ext.tables)
  implementation(libs.markwon.ext.tasklist)
  
  // Testing
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
}
