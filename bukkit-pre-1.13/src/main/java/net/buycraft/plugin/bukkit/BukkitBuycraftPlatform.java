package net.buycraft.plugin.bukkit;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public class BukkitBuycraftPlatform extends BukkitBuycraftPlatformBase {
    private static final ImmutableSet<Material> SIGN_MATERIALS = ImmutableSet.of(Material.SIGN_POST, Material.WALL_SIGN);

    public BukkitBuycraftPlatform(BuycraftPluginBase plugin) {
        super(plugin);
    }

    @Override
    public Material getPlayerSkullMaterial() {
        return Material.SKULL;
    }

    @Override
    public ImmutableSet<Material> getSignMaterials() {
        return SIGN_MATERIALS;
    }
}
