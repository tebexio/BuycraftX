package net.buycraft.plugin.sponge.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

@RequiredArgsConstructor
public class GUIUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
//        plugin.getViewCategoriesGUI().update();
//        plugin.getCategoryViewGUI().update();
    }
}
