import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val shadowJar: ShadowJar by tasks

plugins {
    id("net.kyori.blossom") version "1.2.0"
}

blossom {
    val constants = "src/main/java/net/buycraft/plugin/sponge/BuycraftPlugin.java"
    replaceToken("SET_BY_MAGIC", project.version, constants)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())

        relocate("okhttp3", "net.buycraft.plugin.internal.okhttp3")
        relocate("okio", "net.buycraft.plugin.internal.okio")
        relocate("retrofit2", "net.buycraft.plugin.internal.retrofit2")
        relocate("org.jetbrains.annotations", "net.buycraft.plugin.internal.jetbrains")
        relocate("net.buycraft.plugin", "net.buycraft.plugin")

        minimize()
    }
}

dependencies {
    shadow(project(":plugin-shared")) {
        exclude("org.jetbrains.annotations", "annotations")
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
    }
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.spongepowered:spongeapi:7.0.0")
}