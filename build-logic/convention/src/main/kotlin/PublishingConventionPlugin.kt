import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            afterEvaluate {
                // Create javadoc jar task (empty for now, can be enhanced with Dokka later)
                val javadocJar = tasks.register<Jar>("javadocJar") {
                    archiveClassifier.set("javadoc")
                    // Empty for now - can add Dokka documentation later
                }

                configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            from(components["release"])
                            
                            groupId = "com.chenge.markdown"
                            artifactId = project.name
                            version = "1.0.0"
                            
                            // Add javadoc artifact (sources are included in release component)
                            artifact(javadocJar)
                            
                            pom {
                                name.set("Markdown SDK - ${project.name}")
                                description.set("A comprehensive Markdown rendering SDK for Android")
                                url.set("https://github.com/yourusername/markdownsdk")
                                
                                licenses {
                                    license {
                                        name.set("The Apache License, Version 2.0")
                                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                    }
                                }
                                
                                developers {
                                    developer {
                                        id.set("yourusername")
                                        name.set("Your Name")
                                        email.set("your.email@example.com")
                                    }
                                }
                                
                                scm {
                                    connection.set("scm:git:git://github.com/yourusername/markdownsdk.git")
                                    developerConnection.set("scm:git:ssh://github.com:yourusername/markdownsdk.git")
                                    url.set("https://github.com/yourusername/markdownsdk/tree/main")
                                }
                            }
                        }
                    }
                    
                    repositories {
                        maven {
                            name = "local"
                            url = uri("${project.rootDir}/repo")
                        }
                        
                        maven {
                            name = "sonatype"
                            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = findProperty("ossrhUsername") as String? ?: ""
                                password = findProperty("ossrhPassword") as String? ?: ""
                            }
                        }
                    }
                }
                
                // 确保任务依赖关系正确
                tasks.withType<GenerateModuleMetadata> {
                    dependsOn(javadocJar)
                }
            }
        }
    }
    
    private val Project.android: com.android.build.gradle.LibraryExtension
        get() = extensions.getByType(com.android.build.gradle.LibraryExtension::class.java)
}