pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
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
    maven { url = uri("https://jitpack.io") }
  }
}

rootProject.name = "markdown-sdk"

// Include build-logic for convention plugins
includeBuild("build-logic")

include(":markdown-core")
include(":markdown-render")
include(":markdown-plugins")
include(":markdown-plugins:table")
include(":markdown-plugins:tasklist")
include(":markdown-plugins:latex")
include(":markdown-plugins:image-glide")
include(":markdown-plugins:html")
include(":markdown-sample")
include(":markdown-debug")
include(":markdown-common")
include(":markdown-engine")
