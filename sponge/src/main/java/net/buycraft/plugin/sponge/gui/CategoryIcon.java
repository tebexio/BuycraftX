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

    private final Category category;

    @Override public ItemStack item() {
        ItemStack is = Sponge.getGame().getRegistry().createBuilder(ItemStack.Builder.class).itemType(ItemTypes.BOOK).quantity(1).build();
        is.offer(Keys.DISPLAY_NAME, Text.of((category.getName().length() <= 32)
                ? category.getName() : category.getName().substring(0, 29) + "..."));
        return is;
    }

    @Override public void onClick(Player clicker) {

    }
}
