package net.buycraft.plugin.sponge.gui.icons;

import net.buycraft.plugin.sponge.gui.GuiView;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Created by meyerzinn on 2/17/16.
 */
public class PackageIcon implements GuiIcon {

    @Override public void onClick(GuiView view, Player clicker) {
        clicker.closeInventory();
    }

}
