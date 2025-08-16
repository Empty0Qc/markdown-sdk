import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
            }

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(8)
            }

            dependencies {
                add("testImplementation", "junit:junit:4.13.2")
            }
        }
    }
}