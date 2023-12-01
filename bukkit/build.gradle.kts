import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":sdk"))
    implementation("it.unimi.dsi:fastutil:8.5.6")
    implementation("com.github.cryptomorin:XSeries:9.3.1") { isTransitive = false }
    implementation("dev.triumphteam:triumph-gui:3.1.2")

    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("dev.dejvokep:boosted-yaml:1.3")
    compileOnly("me.clip:placeholderapi:2.11.3")
}

tasks.named("shadowJar", ShadowJar::class.java) {
    configurations = listOf(project.configurations.runtimeClasspath.get())

    relocate("it.unimi", "io.tebex.plugin.libs.fastutil")
    relocate("okhttp3", "io.tebex.plugin.libs.okhttp3")
    relocate("okio", "io.tebex.plugin.libs.okio")
    relocate("dev.dejvokep.boostedyaml", "io.tebex.plugin.libs.boostedyaml")
    relocate("org.jetbrains.annotations", "io.tebex.plugin.libs.jetbrains")
    relocate("kotlin", "io.tebex.plugin.libs.kotlin")
    relocate("com.github.benmanes.caffeine", "io.tebex.plugin.libs.caffeine")
    relocate("com.google.gson", "io.tebex.plugin.libs.gson")
    minimize()
}

tasks.register("copyToServer", Copy::class.java) {
    from(project.tasks.named("shadowJar").get().outputs)
    into("/Users/charlie/Documents/MCServers/MCServer/plugins")

    // rely on the shadowJar task to build the jar
    dependsOn("shadowJar")
}