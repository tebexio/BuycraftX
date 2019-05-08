package net.buycraft.plugin.bukkit;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BukkitBuycraftPlatform extends BukkitBuycraftPlatformBase {
    private static final ImmutableSet<Material> SIGN_MATERIALS = ImmutableSet.of(Material.SIGN_POST, Material.WALL_SIGN);

    public BukkitBuycraftPlatform(BuycraftPluginBase plugin) {
        super(plugin);
    }

    @Override
    public boolean ensureCompatibleServerVersion() {
        try {
            Class.forName("org.bukkit.entity.Dolphin");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    @Override
    public Material getPlayerSkullMaterial() {
        return Material.SKULL;
    }

    @Override
    public ImmutableSet<Material> getSignMaterials() {
        return SIGN_MATERIALS;
    }

    @Override
    public Material getGUIViewAllMaterial() {
        return Material.BOOK_AND_QUILL;
    }

    @Override
    public ItemStack createItemFromMaterialString(String materialData) {
        if (materialData == null || materialData.trim().length() <= 0) return null;

        Material material;
        short variant = 0;

        if (materialData.matches("^\\d+$")) {
            material = Material.getMaterial(Integer.valueOf(materialData));
        } else if (!materialData.contains(":")) {
            material = Material.matchMaterial(materialData);
        } else {
            String[] parts = materialData.split(":");
            if (parts[0].matches("^\\d+$")) {
                material = Material.getMaterial(Integer.valueOf(parts[0]));
            } else {
                material = Material.matchMaterial(parts[0]);
            }
            variant = Short.valueOf(parts[1]);
        }

        if (material == null) return null;
        return new ItemStack(material, 1, variant);
    }
}
