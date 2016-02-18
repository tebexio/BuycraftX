package net.buycraft.plugin.sponge.gui;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.data.Category;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

/**
 * Created by meyerzinn on 2/15/16.
 */
@AllArgsConstructor
public class CategoryIcon implements GuiIcon {

    private final Node node;

    @Override public void onClick(Player clicker) {

    }
}
