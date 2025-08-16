import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = 34

                defaultConfig {
                    minSdk = 21
                    targetSdk = 34

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
                    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
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

            // Configure Kotlin options
            tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }

            // Configure dependency resolution strategy
            configurations.all {
                resolutionStrategy {
                    force("org.jetbrains:annotations:23.0.0")
                }
            }

            dependencies {
                // Kotlin standard library
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
                
                // Android core dependencies
                add("implementation", "androidx.core:core-ktx:1.12.0")
                add("implementation", "androidx.appcompat:appcompat:1.7.0")
                
                // Testing dependencies
                add("testImplementation", "junit:junit:4.13.2")
                add("androidTestImplementation", "androidx.test.ext:junit:1.1.5")
                add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.5.1")
            }
        }
    }
}