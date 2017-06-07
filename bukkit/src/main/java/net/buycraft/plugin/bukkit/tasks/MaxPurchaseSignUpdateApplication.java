package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.util.BukkitSerializedBlockLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

import java.util.List;
import java.util.Map;
import net.buycraft.plugin.data.MaxPayment;
import net.buycraft.plugin.shared.config.signs.storage.MaxPurchaseSignPosition;

@RequiredArgsConstructor
public class MaxPurchaseSignUpdateApplication implements Runnable {
    public static final BlockFace[] FACES = {BlockFace.SELF, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final String UNKNOWN_USERNAME = "MHF_Question";
    private final BuycraftPlugin plugin;
    private final Map<MaxPurchaseSignPosition, MaxPayment> signToPurchases;

    private static Block findSkullBlock(Block origin) {
        Block at = origin.getRelative(BlockFace.UP);
        for (BlockFace face : FACES) {
            Block b = at.getRelative(face);
            if (b.getType() == Material.SKULL)
                return b;
        }
        return null;
    }

    @Override
    public void run() {
        for (Map.Entry<MaxPurchaseSignPosition, MaxPayment> entry : signToPurchases.entrySet()) {
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
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();

                if (entry.getValue() != null) {
                    List<String> lines = plugin.getMaxPurchaseSignLayout().format(entry.getValue());
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
