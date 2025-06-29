plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("maven-publish")
}

android {
  namespace = "com.chenge.markdown.sample"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.chenge.markdown.sample"
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
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
  implementation("com.google.android.material:material:1.11.0")
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-livedata-core:2.8.0")
  implementation("androidx.lifecycle:lifecycle-runtime:2.8.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
  implementation("androidx.profileinstaller:profileinstaller:1.3.1")
  implementation("androidx.activity:activity:1.10.1")
  implementation("androidx.activity:activity-ktx:1.10.1")
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation(platform("androidx.compose:compose-bom:2023.03.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.appcompat:appcompat:1.7.1")

  testImplementation("junit:junit:4.13.2")

  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  implementation(project(":markdown-render"))
  implementation(project(":markdown-plugins"))
  implementation(project(":markdown-core"))
  implementation(project(":markdown-debug"))
  implementation(project(":markdown-common"))
}
