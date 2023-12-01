import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("fabric-loom") version "1.0-SNAPSHOT" apply false
}

defaultTasks("shadowJar")

group = "io.tebex"
version = "2.0.0"

subprojects {
    plugins.apply("java")
    plugins.apply("com.github.johnrengelman.shadow")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.named("shadowJar", ShadowJar::class.java) {
        archiveFileName.set("tebex-${project.name}-${rootProject.version}.jar")
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            name = "spigotmc-repo"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }
        maven("https://repo.opencollab.dev/main/") {
            name = "opencollab-snapshot-repo"
        }
        maven("https://nexus.velocitypowered.com/repository/maven-public/") {
            name = "velocity-repo"
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            name = "extendedclip-repo"
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-snapshots"
        }
        maven("https://maven.nucleoid.xyz/") {
            name = "nucleoid"
        }
    }

    tasks.named("processResources", Copy::class.java) {
        val props = mapOf("version" to rootProject.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        filesNotMatching("**/*.zip") {
            expand(props)
        }
    }
}

val fabricProject = project(":fabric")
fabricProject.configure<JavaPluginExtension> {
    sourceSets {
        getByName("main") {
            java {
                srcDir("src/main/kotlin")
            }
        }
    }
}