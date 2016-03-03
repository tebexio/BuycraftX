package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;

@RequiredArgsConstructor
public class GUIUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        plugin.getViewCategoriesGUI().update();
        plugin.getCategoryViewGUI().update();
    }
}
