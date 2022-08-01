import org.apache.tools.ant.filters.ReplaceTokens

defaultTasks("shadowJar")

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    group = "net.buycraft"
    version = "12.0.8"

    tasks {
        shadowJar {
            archiveFileName.set("buycraftx-${project.name}.jar")
        }

        compileJava {
            options.encoding = "UTF-8"
        }

        processResources {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            filesNotMatching(listOf("**/*.zip", "**/*.properties")) {
                expand("pluginVersion" to version)
            }
        }
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/")
        maven("https://repo.nukkitx.com/maven-snapshots/")
        maven("https://repo.spongepowered.org/maven")
        mavenLocal()
    }

    dependencies {
        implementation("org.jetbrains:annotations:16.0.2")
    }
}