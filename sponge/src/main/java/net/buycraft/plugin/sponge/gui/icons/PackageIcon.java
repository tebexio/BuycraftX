package net.buycraft.plugin.sponge.gui.icons;

import lombok.Getter;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.gui.GuiView;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import org.spongepowered.api.entity.living.player.Player;

public class PackageIcon extends GuiIcon {
    @Getter
    private final Package aPackage;

    public PackageIcon(BuycraftPlugin plugin, GuiView view, Package represent) {
        super(plugin, view);
        aPackage = represent;
    }


    @Override
    public void onClick(GuiView view, Player clicker) {
        view.destroy();
        new SendCheckoutLinkTask(getPlugin(), aPackage.getId(), clicker);
    }
}
