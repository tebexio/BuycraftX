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
    implementation("org.slf4j:slf4j-jdk14:1.7.36")
    implementation("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT") {
        exclude("com.google.guava")
        exclude("io.netty")
    }
    implementation("io.netty:netty-all:4.1.77.Final")
}