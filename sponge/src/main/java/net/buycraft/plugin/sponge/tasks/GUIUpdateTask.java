package net.buycraft.plugin.sponge.tasks;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

/**
 * Created by meyerzinn on 2/14/16.
 */
@RequiredArgsConstructor
public class GUIUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
//        plugin.getViewCategoriesGUI().update();
//        plugin.getCategoryViewGUI().update();
    }
}
