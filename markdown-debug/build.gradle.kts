plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("maven-publish")
}

android {
  namespace = "com.chenge.markdown.debug"
  compileSdk = 35

  defaultConfig {
    minSdk = 21
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  implementation(project(":markdown-render"))
  implementation("io.noties.markwon:core:4.6.2")
  implementation("io.noties.markwon:ext-tables:4.6.2")
  implementation("io.noties.markwon:ext-tasklist:4.6.2")
}
