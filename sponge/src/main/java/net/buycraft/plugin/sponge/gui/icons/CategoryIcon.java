package net.buycraft.plugin.sponge.gui.icons;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.gui.GuiView;
import net.buycraft.plugin.sponge.gui.Node;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Created by meyerzinn on 2/15/16.
 */
@AllArgsConstructor
public class CategoryIcon implements GuiIcon {

    private final Node node;

    @Override public void onClick(GuiView view, Player clicker) {

    }
}
