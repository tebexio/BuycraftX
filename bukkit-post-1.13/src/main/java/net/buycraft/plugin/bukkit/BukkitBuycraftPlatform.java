package net.buycraft.plugin.bukkit;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BukkitBuycraftPlatform extends BukkitBuycraftPlatformBase {
    private static final ImmutableSet<Material> SIGN_MATERIALS = ImmutableSet.copyOf(Arrays.stream(Material.values())
            .filter(material -> !material.name().startsWith("LEGACY_"))
            .filter(material -> material.name().endsWith("SIGN")) // 1.14 has multiple sign variants now
            .filter(Material::isBlock)
            .collect(Collectors.toSet()));

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
        if (materialData == null || materialData.trim().length() <= 0) return null;

        materialData = materialData.split("\\[")[0].split(":")[0];

        Material material = Material.matchMaterial(materialData);
        if (material == null) {
            material = Material.matchMaterial(materialData, true);
        }

        if (material == null) return null;
        return new ItemStack(material);
    }

}
