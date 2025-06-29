plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("maven-publish")
}

android {
  namespace = "com.chenge.markdown.plugins"
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
  implementation(project(":markdown-common"))
  api("io.noties.markwon:core:4.6.2")
  api("io.noties.markwon:ext-tables:4.6.2")
  api("io.noties.markwon:ext-tasklist:4.6.2")
  api("io.noties.markwon:ext-latex:4.6.2")
  api("io.noties.markwon:inline-parser:4.6.2")
  api("io.noties.markwon:ext-strikethrough:4.6.2")
  api("io.noties.markwon:image:4.6.2")
  api("com.github.bumptech.glide:glide:4.15.1")
  api("io.noties.markwon:image-glide:4.6.2")
  api("io.noties.markwon:html:4.6.2")
}

afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        from(components["release"])
        groupId = property("SDK_GROUP") as String
        artifactId = project.name
        version = property("SDK_VERSION") as String
      }
    }
    repositories {
      maven {
        name = "localRepo"
        url = uri("$rootDir/repo")
      }
    }
  }
}