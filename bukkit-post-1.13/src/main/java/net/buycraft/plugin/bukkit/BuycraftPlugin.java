package net.buycraft.plugin.bukkit;

public class BuycraftPlugin extends BuycraftPluginBase {
    @Override
    protected BukkitBuycraftPlatformBase createBukkitPlatform() {
        return new BukkitBuycraftPlatform(this);
    }
}
