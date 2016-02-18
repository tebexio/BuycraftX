package net.buycraft.plugin.sponge.gui;

import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.custom.CustomInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.inventory.type.Inventory2D;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translatable;
import org.spongepowered.api.text.translation.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by meyerzinn on 2/15/16.
 */
public class GuiView {

    /*
    * This class makes several assumptions about the way the system works:
    * 1. A view is just a collection of subcategories/packages ordered by their given weight.
    * 2. A view can be of variable size (multiples of 9) but not exceeding 45.
    * 3. The final row (6) is reserved for pagination tools if relevant and an information item.
    * 4. Packages and subcategories are on a "tree", beginning with the root category.
    * In order for assumption 4 to be fully correct, a field, "parent", must be added to the Package and
     */

    @Getter
    private BuycraftPlugin plugin;
    @Getter
    private Node node;
    @Getter
    private int page;
    @Getter
    private Player viewer;
    @Getter
    @Setter
    private CustomInventory inventory;
    private Map<Slot, GuiIcon> iconHandlers = new HashMap<>();

    public GuiView(BuycraftPlugin plugin, Node node, Player viewer) {
        this.plugin = plugin;
        this.node = node;
        this.viewer = viewer;
    }

    public void update() {
        if (needsResize()) {
            inventory = CustomInventory.builder()
                    .name(getFixedTranslation()).size(getSize()).build();
        }
        iconHandlers.clear();
        for (int i = 0; i < node.getSubcategories().size(); i++) {
            Category category = node.getSubcategories().get(i);
            ItemStack c = ItemStack.builder().itemType(ItemTypes.BOOK).build();
            if (c.get(Keys.DISPLAY_NAME).isPresent()) {
                c.offer(Keys.DISPLAY_NAME, Text.builder(category.getName()).color(TextColors.YELLOW).build());
            }
            if (inventory.getSlot(new SlotIndex(i)).isPresent()) {
                inventory.getSlot(new SlotIndex(i)).get().set(c);
                iconHandlers.put(inventory.getSlot(new SlotIndex(i)).get(), new CategoryIcon())
            }
        }
        for (int i = 0; i < node.getPackages().size(); i++) {
            Package p = node.getPackages().get(i);
            ItemStack c = ItemStack.builder().itemType(ItemTypes.PAPER).build();
            if (c.get(Keys.DISPLAY_NAME).isPresent()) {
                c.offer(Keys.DISPLAY_NAME, Text.builder(p.getName()).color(TextColors.GREEN).build());
            }
            if (c.get(Keys.ITEM_LORE).isPresent()) {
                c.offer(Keys.ITEM_LORE, new ArrayList<>(Arrays.asList(Text.builder("Price: ").color(TextColors.GRAY).append(Text.builder(plugin
                        .getServerInformation().getAccount().getCurrency().getSymbol() + p.getEffectivePrice()).color(TextColors.GREEN).build())
                        .build())));
            }
            if (inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).isPresent()) {
                inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).get().set(c);
                iconHandlers.put(inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).get(), new PackageIcon());
            }
        }

    }

    private FixedTranslation getFixedTranslation() {
        return node.getTranslatableTitle();
    }


    /**
     * Gets the size the final inventory should be. This includes the footer bar.
     * @return
     */
    private int getSize() {
        return roundNine(node.getPackages().size() + node.getSubcategories().size()) + 9;
    }

    private int roundNine(int max) {
        if (max <= 0) {
            return 9;
        }
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 5 ? 45 : quotient * 9;
    }

    private boolean needsResize() {
        return getSize() == inventory.size();
    }

    public void close() {
        viewer.closeInventory();
        Sponge.getEventManager().unregisterListeners(this);
    }

    public void open() {
        viewer.openInventory(inventory);
        update();
    }

    public void destroy() {
        close();
    }

    @Listener
    public void onChangeInventoryEvent(ChangeInventoryEvent event) {
        if (event.getTargetInventory().parent().equals(inventory)) {
            event.setCancelled(true);
            event.getTransactions().get(0).getSlot();
            if (event.getCause().first(Player.class).isPresent()) {

            }
        }
    }

    @Listener
    public void onPlayerQuitEvent(ClientConnectionEvent event) {
        if (event == viewer || !viewer.isOnline()) {

        }
    }

}
