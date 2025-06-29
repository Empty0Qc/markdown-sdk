plugins {
  kotlin("jvm")
  id("maven-publish")
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(kotlin("stdlib"))
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
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
