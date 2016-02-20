package net.buycraft.plugin.sponge.gui.icons;

import net.buycraft.plugin.sponge.gui.GuiView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Created by meyerzinn on 2/15/16.
 */
public interface GuiIcon {

    void onClick(GuiView view, Player clicker);

}
