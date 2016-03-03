package net.buycraft.plugin.sponge.gui.icons;

import lombok.Getter;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.gui.GuiView;
import net.buycraft.plugin.util.Node;
import org.spongepowered.api.entity.living.player.Player;

public class CategoryIcon extends GuiIcon {

    @Getter
    private final Node node;

    public CategoryIcon(BuycraftPlugin plugin, GuiView view, Node node) {
        super(plugin, view);
        this.node = node;
    }

    @Override
    public void onClick(GuiView view, Player clicker) {
        view.setNode(node);
        view.setPage(0);
        view.update();
    }
}
