import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

val shadowJar: ShadowJar by tasks

tasks {
    build {
        dependsOn(shadowJar)
    }

    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())

        relocate("com.google.gson", "net.buycraft.plugin.internal.gson")
        relocate("okhttp3", "net.buycraft.plugin.internal.okhttp3")
        relocate("okio", "net.buycraft.plugin.internal.okio")
        relocate("retrofit2", "net.buycraft.plugin.internal.retrofit2")
        relocate("com.fasterxml.jackson", "net.buycraft.plugin.internal.jackson")
        relocate("org.slf4j", "net.buycraft.plugin.internal.slf4j")
        relocate("net.buycraft.plugin", "net.buycraft.plugin")

        minimize()
    }
}

dependencies {
    shadow(project(":bukkit-shared"))
    implementation("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
}