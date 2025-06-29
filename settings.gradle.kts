pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }

  plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.4.0"
    id("com.android.library") version "8.4.0"

    // Kotlin Plugin
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "markdown-sdk"
include(":markdown-core")
include(":markdown-render")
include(":markdown-plugins")
include(":markdown-sample")
include(":markdown-debug")
include(":markdown-common")
