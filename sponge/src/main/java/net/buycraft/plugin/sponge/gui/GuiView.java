package net.buycraft.plugin.sponge.gui;

import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.gui.icons.*;
import net.buycraft.plugin.util.Node;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.custom.CustomInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.translation.FixedTranslation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    @Setter
    private Node node;
    @Getter
    @Setter
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
        Sponge.getEventManager().registerListeners(getPlugin(), this);
    }

    public void update() {
        // If we need to resize the inventory, do so now.
        if (needsResize()) {
            inventory = CustomInventory.builder()
                    .name(getFixedTranslation()).size(getSize() + 9).build();
        }
        // Clear the Icon Handlers.
        iconHandlers.clear();
        // j represents the subcategory to start at.
        int j = page * 45;
        for (int i = 0; j < node.getSubcategories().size() && i < getSize(); i++) {
            // Iterate through the working slots and fill them up with subcategories.
            Category category = node.getSubcategories().get(j);
            ItemStack c = ItemStack.builder().itemType(ItemTypes.BOOK).build();
            if (c.supports(Keys.DISPLAY_NAME)) {
                c.offer(Keys.DISPLAY_NAME, Text.builder(category.getName()).color(TextColors.YELLOW).build());
            }
            if (inventory.getSlot(new SlotIndex(i)).isPresent()) {
                inventory.getSlot(new SlotIndex(i)).get().set(c);
                iconHandlers.put(inventory.getSlot(new SlotIndex(i)).get(), new CategoryIcon(plugin, this, node.getChild(category)));
            }
            j++;
        }
        // k represents the package to start at.
        int k = page * 45;
        for (int i = 0; k < node.getPackages().size() && i < 45; i++) {
            // Iterate through the remaining working slots and fill them up with packages.
            Package p = node.getPackages().get(k);
            ItemStack c = ItemStack.builder().itemType(ItemTypes.PAPER).build();
            if (c.supports(Keys.DISPLAY_NAME)) {
                c.offer(Keys.DISPLAY_NAME, Text.builder(p.getName()).color(TextColors.GREEN).build());
            }
            if (c.supports(Keys.ITEM_LORE)) {
                c.offer(Keys.ITEM_LORE, new ArrayList<>(Arrays.asList(Text.builder("Price: ").color(TextColors.GRAY).append(Text.builder(plugin
                        .getServerInformation().getAccount().getCurrency().getSymbol() + p.getEffectivePrice()).color(TextColors.DARK_GREEN)
                        .style(TextStyles.BOLD).build()).build())));
            }
            if (inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).isPresent()) {
                inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).get().set(c);
                iconHandlers.put(inventory.getSlot(new SlotIndex(i + node.getSubcategories().size())).get(), new PackageIcon(plugin, this, node
                        .getPackages().get(k)));
            }
            k++;
        }
        if (page > 0) {
            if (inventory.getSlot(new SlotIndex(getSize() + 1)).isPresent()) {
                ItemStack b = ItemStack.builder().itemType(ItemTypes.NETHER_STAR).quantity(1).build();
                if (b.supports(Keys.DISPLAY_NAME)) {
                    b.offer(Keys.DISPLAY_NAME, Text.builder("Previous Page").color(TextColors.AQUA).build());
                }
                inventory.getSlot(new SlotIndex(getSize() + 1)).get().set(b);
                iconHandlers
                        .put(inventory.getSlot(new SlotIndex(getSize() + 1)).get(), new NavigationIcon(plugin, this, NavigationDirection.BACKWARD));
            }
        }
        if (page * 45 < node.getPackages().size() + node.getSubcategories().size()) {
            if (inventory.getSlot(new SlotIndex(getSize() + 7)).isPresent()) {
                ItemStack b = ItemStack.builder().itemType(ItemTypes.NETHER_STAR).quantity(1).build();
                if (b.supports(Keys.DISPLAY_NAME)) {
                    b.offer(Keys.DISPLAY_NAME, Text.builder("Next Page").color(TextColors.AQUA).build());
                }
                inventory.getSlot(new SlotIndex(getSize() + 7)).get().set(b);
                iconHandlers
                        .put(inventory.getSlot(new SlotIndex(getSize() + 7)).get(), new NavigationIcon(plugin, this, NavigationDirection.FORWARD));
            }
        }
        if (inventory.getSlot(new SlotIndex(getSize() + 4)).isPresent()) {
            ItemStack i = ItemStack.builder().itemType(ItemTypes.WRITABLE_BOOK).quantity(4).build();
            if (i.supports(Keys.DISPLAY_NAME)) {
                i.offer(Keys.DISPLAY_NAME, Text.builder((node.getParent().isPresent()) ? "Back to Parent" : "Close").color(TextColors.GRAY).build());
            }
            inventory.getSlot(new SlotIndex(getSize() + 4)).get().set(i);
            iconHandlers.put(inventory.getSlot(new SlotIndex(getSize() + 4)).get(), new NavigationIcon(getPlugin(), this, NavigationDirection.UP));
        }
    }

    private FixedTranslation getFixedTranslation() {
        return new FixedTranslation(node.getTitle());
    }

    /**
     * Gets the working size the inventory should be. This does not include the footer bar.
     *
     * @return
     */
    private int getSize() {
        return roundNine(node.getPackages().size() + node.getSubcategories().size());
    }


    private int roundNine(int max) {
        if (max <= 0) {
            return 9;
        }
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 4 ? 45 : quotient * 9;
    }

    private boolean needsResize() {
        return getSize() == inventory.size() - 9;
    }

    public void close() {
        viewer.closeInventory(Cause.of(NamedCause.of("FORCED", plugin)));
        iconHandlers.clear();
        Sponge.getEventManager().unregisterListeners(this);
    }

    public void open() {
        viewer.openInventory(inventory, Cause.of(NamedCause.of("GUI", plugin)));
        update();
    }

    public void destroy() {
        close();
    }

    @Listener
    public void onClickInventoryEvent(ClickInventoryEvent event) {
        if (event.getTargetInventory() instanceof Slot) {
            System.out.println("Inventory was an instance of a slot.");
            Slot slot = (Slot) event.getTargetInventory();
            iconHandlers.get(slot).onClick(this, viewer);
        } else {
            System.out.println("Inventory was not an instance of a slot.");
        }
    }
}
