package net.buycraft.plugin.sponge.gui.icons;

import lombok.Getter;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.gui.GuiView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.concurrent.Callable;

/**
 * Created by meyerzinn on 2/15/16.
 */
public abstract class GuiIcon {

    @Getter
    private final BuycraftPlugin plugin;
    @Getter
    private final GuiView view;

    public GuiIcon(BuycraftPlugin plugin, GuiView view) {
        this.plugin = plugin;
        this.view = view;
    }

    public abstract void onClick(GuiView view, Player clicker);

}
