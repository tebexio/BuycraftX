package io.tebex.plugin.gui;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.tebex.plugin.TebexPlugin;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.CategoryPackage;
import io.tebex.sdk.obj.ICategory;
import io.tebex.sdk.obj.SubCategory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BuyGUI {
    private final TebexPlugin platform;
    private final YamlDocument config;

    public BuyGUI(TebexPlugin platform) {
        this.platform = platform;
        this.config = platform.getPlatformConfig().getYamlDocument();
    }

    private ScreenHandlerType<?> getRowType(final int rows) {
        ScreenHandlerType<?> type;
        switch (rows) {
            case 1 -> type = ScreenHandlerType.GENERIC_9X1;
            case 2 -> type = ScreenHandlerType.GENERIC_9X2;
            case 3 -> type = ScreenHandlerType.GENERIC_9X3;
            case 4 -> type = ScreenHandlerType.GENERIC_9X4;
            case 5 -> type = ScreenHandlerType.GENERIC_9X5;
            default -> type = ScreenHandlerType.GENERIC_9X6;
        }
        return type;
    }

    private String convertToLegacyString(String str) {
        return str.replace("&", "§");
    }

    public void open(ServerPlayerEntity player) {
        List<Category> categories = platform.getStoreCategories();
        if (categories == null) {
            player.sendMessage(new LiteralText("Failed to get listing. Please contact an administrator."), false);
            return;
        }

        int rows = config.getInt("gui.menu.home.rows") < 1 ? categories.size() / 9 + 1 : config.getInt("gui.menu.home.rows");
        SimpleGui listingGui = new SimpleGui(getRowType(rows), player, false);
        listingGui.setTitle(new LiteralText(convertToLegacyString(config.getString("gui.menu.home.title", "Server Shop"))));

        categories.sort(Comparator.comparingInt(Category::getOrder));

        categories.forEach(category -> listingGui.addSlot(getCategoryItemBuilder(category).setCallback((index, clickType, actionType) -> {
                    listingGui.close();
                    openCategoryMenu(player, category);
                }
        )));

        platform.executeBlocking(listingGui::open);
    }

    private void openCategoryMenu(ServerPlayerEntity player, ICategory category) {
        int rows = config.getInt("gui.menu.category.rows") < 1 ? category.getPackages().size() / 9 + 1 : config.getInt("gui.menu.category.rows");

        SimpleGui subListingGui = new SimpleGui(getRowType(rows), player, false);
        subListingGui.setTitle(new LiteralText(convertToLegacyString(config.getString("gui.menu.category.title").replace("%category%", category.getName()))));

        category.getPackages().sort(Comparator.comparingInt(CategoryPackage::getOrder));

        if (category instanceof Category cat) {
            if (cat.getSubCategories() != null) {
                cat.getSubCategories().forEach(subCategory -> subListingGui.addSlot(getCategoryItemBuilder(subCategory).setCallback((index, clickType, actionType) -> {
                    openCategoryMenu(player, subCategory);
                })));

                subListingGui.setSlot((rows*9) - 5, getBackItemBuilder()
                        .setCallback((index, clickType, actionType) -> {
                            open(player);
                        })
                );
            }
        } else if (category instanceof SubCategory) {
            SubCategory subCategory = (SubCategory) category;

            subListingGui.setTitle(new LiteralText(convertToLegacyString(config.getString("gui.menu.sub-category.title"))
                    .replace("%category%", subCategory.getParent().getName())
                    .replace("%sub_category%", category.getName())));

            subListingGui.setSlot((rows*9) - 5, getBackItemBuilder()
                    .setCallback((index, clickType, actionType) -> {
                        openCategoryMenu(player, subCategory.getParent());
                    })
            );
        }

        category.getPackages().forEach(categoryPackage -> subListingGui.addSlot(getPackageItemBuilder(categoryPackage).setCallback((index, clickType, actionType) -> {
            player.closeHandledScreen();

            // Create Checkout Url
            platform.getSDK().createCheckoutUrl(categoryPackage.getId(), player.getName().asString()).thenAccept(checkout -> {
                player.sendMessage(new LiteralText("§aYou can checkout here: " + checkout.getUrl()), false);
            }).exceptionally(ex -> {
                player.sendMessage(new LiteralText("§cFailed to create checkout URL. Please contact an administrator."), false);
                ex.printStackTrace();
                platform.sendTriageEvent(ex);
                return null;
            });
        })));

        subListingGui.open();
    }

    private GuiElementBuilder getCategoryItemBuilder(ICategory category) {
        Section section = config.getSection("gui.item.category");

        String itemType = section.getString("material");
        Item material = Registry.ITEM.get(Identifier.tryParse(itemType.toLowerCase()));

        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");

        return new GuiElementBuilder(material.asItem() != null ? material : Items.BOOK)
                .setName(new LiteralText(convertToLegacyString(name != null ? handlePlaceholders(category, name) : category.getName())).setStyle(Style.EMPTY.withItalic(true)))
                .setLore(lore.stream().map(line -> new LiteralText(convertToLegacyString(handlePlaceholders(category, line))).setStyle(Style.EMPTY.withItalic(true))).collect(Collectors.toList()))
                .hideFlag(ItemStack.TooltipSection.ENCHANTMENTS)
                .hideFlag(ItemStack.TooltipSection.UNBREAKABLE)
                .hideFlag(ItemStack.TooltipSection.ADDITIONAL);
    }

    private GuiElementBuilder getPackageItemBuilder(CategoryPackage categoryPackage) {
        Section section = config.getSection("gui.item." + (categoryPackage.hasSale() ? "package-sale" : "package"));

        if (section == null) {
            platform.warning("Invalid configuration section for " + (categoryPackage.hasSale() ? "package-sale" : "package"));
            return null;
        }

        String itemType = section.getString("material");
        Item material = Registry.ITEM.get(Identifier.tryParse(itemType.toLowerCase()));

        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");

        GuiElementBuilder guiElementBuilder = new GuiElementBuilder(material.asItem() != null ? material : Items.BOOK)
                .setName(new LiteralText(convertToLegacyString(name != null ? handlePlaceholders(categoryPackage, name) : categoryPackage.getName())).setStyle(Style.EMPTY.withItalic(true)))
                .setLore(lore.stream().map(line -> new LiteralText(convertToLegacyString(handlePlaceholders(categoryPackage, line))).setStyle(Style.EMPTY.withItalic(true))).collect(Collectors.toList()))
                .hideFlag(ItemStack.TooltipSection.ENCHANTMENTS)
                .hideFlag(ItemStack.TooltipSection.UNBREAKABLE)
                .hideFlag(ItemStack.TooltipSection.ADDITIONAL);

        if (categoryPackage.hasSale()) {
            guiElementBuilder.enchant(Enchantment.byRawId(0), 1);
        }

        return guiElementBuilder;
    }

    private GuiElementBuilder getBackItemBuilder() {
        Section section = config.getSection("gui.item.back");

        String itemType = section.getString("material");
        Item material = Registry.ITEM.get(Identifier.tryParse(itemType.toLowerCase()));

        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");

        return new GuiElementBuilder(material.asItem() != null ? material : Items.BOOK)
                .setName(new LiteralText(convertToLegacyString(name != null ? name : "§fBack")))
                .setLore(lore.stream().map(line -> new LiteralText(convertToLegacyString(line)).setStyle(Style.EMPTY.withItalic(true))).collect(Collectors.toList()))
                .hideFlag(ItemStack.TooltipSection.ENCHANTMENTS)
                .hideFlag(ItemStack.TooltipSection.UNBREAKABLE)
                .hideFlag(ItemStack.TooltipSection.ADDITIONAL);
    }

    private String handlePlaceholders(Object obj, String str) {
        if (obj instanceof ICategory category) {
            str = str.replace("%category%", category.getName());
        } else if (obj instanceof CategoryPackage categoryPackage) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            str = str
                    .replace("%package_name%", categoryPackage.getName())
                    .replace("%package_price%", decimalFormat.format(categoryPackage.getPrice()))
                    .replace("%package_currency_name%", platform.getStoreInformation().getStore().getCurrency().getIso4217())
                    .replace("%package_currency%", platform.getStoreInformation().getStore().getCurrency().getSymbol());

            if (categoryPackage.hasSale()) {
                str = str
                        .replace("%package_discount%", decimalFormat.format(categoryPackage.getSale().getDiscount()))
                        .replace("%package_sale_price%", decimalFormat.format(categoryPackage.getPrice() - categoryPackage.getSale().getDiscount()));
            }
        }

        return str;
    }
}
