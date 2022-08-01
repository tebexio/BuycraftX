import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

val shadowJar: ShadowJar by tasks

tasks {
    build {
        dependsOn(shadowJar)
    }

    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())

        relocate("com.google.gson", "net.buycraft.plugin.internal.gson")
        relocate("com.google.common", "net.buycraft.plugin.internal.common")
        relocate("okhttp3", "net.buycraft.plugin.internal.okhttp3")
        relocate("okio", "net.buycraft.plugin.internal.okio")
        relocate("retrofit2", "net.buycraft.plugin.internal.retrofit2")
        relocate("com.fasterxml.jackson", "net.buycraft.plugin.internal.jackson")
        relocate("org.slf4j", "net.buycraft.plugin.internal.slf4j")
        relocate("org.jetbrains.annotations", "net.buycraft.plugin.internal.jetbrains")
        relocate("net.buycraft.plugin", "net.buycraft.plugin")

        minimize()
    }
}

dependencies {
    shadow(project(":plugin-shared"))
    implementation("cn.nukkit:nukkit:1.0-SNAPSHOT")
}