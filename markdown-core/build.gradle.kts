plugins {
    alias(libs.plugins.markdownsdk.android.library)
    alias(libs.plugins.markdownsdk.publishing)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.chenge.markdown.core"
}

dependencies {
    implementation(project(":markdown-common"))

    // Test dependencies
    testImplementation(libs.junit)
}
