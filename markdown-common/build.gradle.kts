plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("maven-publish")
}


android {
  namespace = "com.chenge.markdown.common"
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
