package net.buycraft.plugin.bukkit.tasks;

import net.buycraft.plugin.bukkit.BuycraftPluginBase;

public class GUIUpdateTask implements Runnable {
    private final BuycraftPluginBase plugin;

    public GUIUpdateTask(final BuycraftPluginBase plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getViewCategoriesGUI().update();
        plugin.getCategoryViewGUI().update();
    }
}
