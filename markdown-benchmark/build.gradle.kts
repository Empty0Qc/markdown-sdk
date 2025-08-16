plugins {
    alias(libs.plugins.markdownsdk.android.library)
    alias(libs.plugins.androidx.benchmark)
}

android {
    namespace = "com.markdownsdk.benchmark"
    testBuildType = "release"
    defaultConfig {
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }
}

dependencies {
    implementation(project(":markdown-engine"))
    implementation(project(":markdown-core"))
    
    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}