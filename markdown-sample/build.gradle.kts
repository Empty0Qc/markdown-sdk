plugins {
  alias(libs.plugins.markdownsdk.android.application)
}

android {
  namespace = "com.chenge.markdown.sample"

  defaultConfig {
    applicationId = "com.chenge.markdown.sample"
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.8"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(libs.material)
  implementation(libs.androidx.core.ktx)
  implementation(libs.bundles.androidx.lifecycle)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.androidx.compose)

  testImplementation(libs.junit)

  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)

  debugImplementation(libs.bundles.androidx.compose.debug)

  implementation(project(":markdown-engine"))
  implementation(project(":markdown-debug"))
  // 添加 common 依赖以确保 MarkdownConfig/MarkdownSanitizer 可用
  implementation(project(":markdown-common"))
}
