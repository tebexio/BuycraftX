import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = rootProject.group
version = rootProject.version

plugins {
    java
    id("com.github.johnrengelman.shadow")
    id("fabric-loom")
}

var minecraftVersion = properties["minecraft_version"] as String
var yarnMappings = properties["yarn_mappings"] as String
var loaderVersion = properties["loader_version"] as String
var fabricVersion = properties["fabric_version"] as String

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

dependencies {
    shadow(project(":sdk"))
    shadow("it.unimi.dsi:fastutil:8.5.6")
    shadow("com.github.cryptomorin:XSeries:9.3.1") {
        isTransitive = false
    }

    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")

    modImplementation("eu.pb4:sgui:0.5.0")
    include("eu.pb4:sgui:0.5.0")

    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    compileOnly("dev.dejvokep:boosted-yaml:1.3")
}


tasks.named("shadowJar", ShadowJar::class.java) {
    configurations = listOf(project.configurations.shadow.get())

    relocate("it.unimi", "io.tebex.plugin.libs.fastutil")
    relocate("okhttp3", "io.tebex.plugin.libs.okhttp3")
    relocate("okio", "io.tebex.plugin.libs.okio")
    relocate("dev.dejvokep.boostedyaml", "io.tebex.plugin.libs.boostedyaml")
    relocate("org.jetbrains.annotations", "io.tebex.plugin.libs.jetbrains")
    relocate("kotlin", "io.tebex.plugin.libs.kotlin")
    relocate("com.github.benmanes.caffeine", "io.tebex.plugin.libs.caffeine")
    relocate("com.google.gson", "io.tebex.plugin.libs.gson")
    minimize()

    archiveFileName.set("${project.name}-${project.version}-shadow.jar")

    finalizedBy("remapJar")
}

tasks.remapJar {
    dependsOn("shadowJar")
    val shadowJar = tasks.shadowJar.get()

    inputFile.set(shadowJar.archiveFile)
    archiveFileName.set("tebex-${project.name}-${project.version}.jar")
    archiveClassifier.set(shadowJar.archiveClassifier)
    delete(shadowJar.archiveFile)
}