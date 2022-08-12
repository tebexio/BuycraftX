rootProject.name = "BuycraftX"

pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}


listOf("common", "bukkit-shared", "plugin-shared", "bukkit-pre-1.13", "bukkit-post-1.13", "bungeecord", "nukkit", "sponge", "fabric").forEach { include(it) }