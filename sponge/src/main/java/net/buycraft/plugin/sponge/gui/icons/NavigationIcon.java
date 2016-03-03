package net.buycraft.plugin.sponge.gui.icons;

import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.gui.GuiView;
import org.spongepowered.api.entity.living.player.Player;

public class NavigationIcon extends GuiIcon {

    private final NavigationDirection direction;

    public NavigationIcon(BuycraftPlugin plugin, GuiView view, NavigationDirection direction) {
        super(plugin, view);
        this.direction = direction;
    }

    @Override
    public void onClick(GuiView view, Player clicker) {
        switch (direction) {
            case FORWARD:
                view.setPage(view.getPage() + 1);
                view.update();
                break;
            case BACKWARD:
                view.setPage(view.getPage() - 1);
                view.update();
                break;
            case UP:
                if (view.getNode().getParent().isPresent()) {
                    view.setNode(view.getNode().getParent().get());
                } else {
                    view.destroy();
                }
                break;
        }
    }
}
