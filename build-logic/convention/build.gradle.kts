import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.chenge.markdown.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.2.2")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    compileOnly("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.4")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "markdownsdk.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "markdownsdk.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "markdownsdk.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("publishing") {
            id = "markdownsdk.publishing"
            implementationClass = "PublishingConventionPlugin"
        }
    }
}