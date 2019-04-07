package net.buycraft.plugin.bukkit.tasks;

import net.buycraft.plugin.bukkit.BuycraftPluginBase;
import net.buycraft.plugin.bukkit.util.BukkitSerializedBlockLocation;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

import java.util.List;
import java.util.Map;

public class RecentPurchaseSignUpdateApplication implements Runnable {
    public static final BlockFace[] FACES = {BlockFace.SELF, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final String UNKNOWN_USERNAME = "MHF_Question";
    private final BuycraftPluginBase plugin;
    private final Map<RecentPurchaseSignPosition, RecentPayment> signToPurchases;

    public RecentPurchaseSignUpdateApplication(final BuycraftPluginBase plugin, final Map<RecentPurchaseSignPosition, RecentPayment> signToPurchases) {
        this.plugin = plugin;
        this.signToPurchases = signToPurchases;
    }

    private Block findSkullBlock(Block origin) {
        Block at = origin.getRelative(BlockFace.UP);
        for (BlockFace face : FACES) {
            Block b = at.getRelative(face);
            if (b.getType() == plugin.getPlatform().getPlayerSkullMaterial()) return b;
        }
        return null;
    }

    @Override
    public void run() {
        for (Map.Entry<RecentPurchaseSignPosition, RecentPayment> entry : signToPurchases.entrySet()) {
            Location location = BukkitSerializedBlockLocation.toBukkit(entry.getKey().getLocation());
            if (location.getWorld() == null) {
                // Invalid (no world).
                continue;
            }
            Block block = location.getBlock();
            if (block == null) {
                // Invalid.
                continue;
            }
            if (plugin.getPlatform().getSignMaterials().contains(block.getType())) {
                Sign sign = (Sign) block.getState();
                if (entry.getValue() != null) {
                    List<String> lines = plugin.getRecentPurchaseSignLayout().format(entry.getValue());
                    for (int i = 0; i < 4; i++) {
                        sign.setLine(i, ChatColor.translateAlternateColorCodes('&', i >= lines.size() ? "" : lines.get(i)));
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        sign.setLine(i, "");
                    }
                }

                sign.update();
                Block skullBlock = findSkullBlock(block);
                if (skullBlock != null) {
                    Skull skull = (Skull) skullBlock.getState();
                    skull.setSkullType(SkullType.PLAYER);
                    skull.setOwner(entry.getValue() == null ? UNKNOWN_USERNAME : entry.getValue().getPlayer().getName());
                    skull.update();
                }
            } else {
                // TODO: Help the user by cleaning it up.
                plugin.getLogger().warning("Location " + entry.getKey().getLocation() + " doesn't have a sign!");
            }
        }
    }
}
