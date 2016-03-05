package net.buycraft.plugin.bukkit.signs.purchases;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SignUpdateApplication;
import net.buycraft.plugin.bukkit.tasks.SignUpdater;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

@RequiredArgsConstructor
public class RecentPurchaseSignListener implements Listener {
    private final BuycraftPlugin plugin;

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        boolean ourSign;
        try {
            ourSign = event.getLine(0).equalsIgnoreCase("[buycraft_rp]");
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!ourSign)
            return;

        if (!event.getPlayer().hasPermission("buycraft.admin")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't create Buycraft signs.");
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(event.getLine(1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "The second line must be a number.");
            return;
        }

        if (pos <= 0) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't show negative or zero purchases!");
            return;
        }

        if (pos > 100) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't show more than 100 recent purchases!");
            return;
        }

        plugin.getRecentPurchaseSignStorage().addSign(new RecentPurchaseSignPosition(SerializedBlockLocation.fromBukkitLocation(
                event.getBlock().getLocation()), pos));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Added new recent purchase sign!");

        for (int i = 0; i < 4; i++) {
            event.setLine(i, "");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new SignUpdater(plugin));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            if (plugin.getRecentPurchaseSignStorage().containsLocation(event.getBlock().getLocation())) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }
                if (plugin.getRecentPurchaseSignStorage().removeSign(event.getBlock().getLocation())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed recent purchase sign!");
                }
            }
            return;
        }

        for (BlockFace face : SignUpdateApplication.FACES) {
            Location onFace = event.getBlock().getRelative(face).getLocation();
            if (plugin.getRecentPurchaseSignStorage().containsLocation(onFace)) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }
                if (plugin.getRecentPurchaseSignStorage().removeSign(onFace)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed recent purchase sign!");
                }
            }
        }
    }
}
