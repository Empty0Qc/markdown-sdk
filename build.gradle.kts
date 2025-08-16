// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  // Android Gradle Plugin
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false

  // Kotlin Android Plugin
  alias(libs.plugins.kotlin.android) apply false

  // Documentation
  alias(libs.plugins.dokka) apply false

  // Code Quality
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.ktlint) apply false
}

// Configure dependency resolution strategy for all projects
allprojects {
  configurations.all {
    resolutionStrategy {
      force("org.jetbrains:annotations:23.0.0")
    }
    exclude(group = "org.jetbrains", module = "annotations-java5")
  }
}

