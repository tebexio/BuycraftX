package net.buycraft.plugin.bukkit;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class BukkitBuycraftPlatform extends BukkitBuycraftPlatformBase {
    private static final ImmutableSet<Material> SIGN_MATERIALS = ImmutableSet.of(Material.SIGN, Material.WALL_SIGN);

    public BukkitBuycraftPlatform(BuycraftPluginBase plugin) {
        super(plugin);
    }

    @Override
    public Material getPlayerSkullMaterial() {
        return Material.PLAYER_HEAD;
    }

    @Override
    public ImmutableSet<Material> getSignMaterials() {
        return SIGN_MATERIALS;
    }

    @Override
    public Material getGUIViewAllMaterial() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public ItemStack createItemFromMaterialString(String materialData) {
        if(materialData == null || materialData.trim().length()<=0) return null;

        Material material;

        if(materialData.contains("[")) {
            material = Material.matchMaterial(materialData.split("\\[")[0]);
        } else {
            material = Material.matchMaterial(materialData.split(":")[0]);
        }

        if(material == null) return null;
        return new ItemStack(material);
    }

}
