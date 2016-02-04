package net.buycraft.plugin.bukkit.signs.buynow;

import lombok.Getter;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.bukkit.tasks.SignUpdateApplication;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;
import net.buycraft.plugin.data.Package;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class BuyNowSignListener implements Listener {
    @Getter
    private final Map<UUID, SerializedBlockLocation> settingUpSigns = new HashMap<>();
    private final BuycraftPlugin plugin;
    private boolean signLimited = false;

    public BuyNowSignListener(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        boolean relevant;
        try {
            relevant = event.getLine(0).equalsIgnoreCase("[buycraft_buy]");
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!relevant)
            return;

        if (!event.getPlayer().hasPermission("buycraft.admin")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to create this sign.");
            return;
        }

        for (int i = 0; i < 4; i++) {
            event.setLine(i, "");
        }

        settingUpSigns.put(event.getPlayer().getUniqueId(), SerializedBlockLocation.fromBukkitLocation(event.getBlock().getLocation()));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Navigate to the item you want to set this sign for.");

        plugin.getViewCategoriesGUI().open(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block b = event.getClickedBlock();

            if (!(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST))
                return;

            SerializedBlockLocation sbl = SerializedBlockLocation.fromBukkitLocation(event.getClickedBlock().getLocation());

            for (SavedBuyNowSign s : plugin.getBuyNowSignStorage().getSigns()) {
                if (s.getLocation().equals(sbl)) {
                    // Signs are rate limited in order to limit API calls issued.
                    if (signLimited) {
                        return;
                    }
                    signLimited = true;

                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, s.getPackageId(),
                            event.getPlayer()));
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            signLimited = false;
                        }
                    }, 4);

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (settingUpSigns.containsKey(event.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if ((event.getPlayer().getOpenInventory().getTopInventory() == null ||
                            !event.getPlayer().getOpenInventory().getTopInventory().getTitle().startsWith("Buycraft: ")) &&
                            settingUpSigns.remove(event.getPlayer().getUniqueId()) != null) {
                        event.getPlayer().sendMessage(ChatColor.RED + "Buy sign set up cancelled.");
                    }
                }
            }, 3);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("buycraft.admin")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
            return;
        }

        if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            if (plugin.getBuyNowSignStorage().removeSign(event.getBlock().getLocation())) {
                event.getPlayer().sendMessage(ChatColor.RED + "Removed buy now sign!");
            }
            return;
        }

        for (BlockFace face : SignUpdateApplication.FACES) {
            if (plugin.getBuyNowSignStorage().removeSign(event.getBlock().getRelative(face).getLocation())) {
                event.getPlayer().sendMessage(ChatColor.RED + "Removed buy now sign!");
                return;
            }
        }
    }

    public void doSignSetup(Player player, Package p) {
        SerializedBlockLocation sbl = settingUpSigns.remove(player.getUniqueId());
        if (sbl == null)
            return;

        Block b = sbl.toBukkitLocation().getBlock();

        if (!(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST))
            return;

        plugin.getBuyNowSignStorage().addSign(new SavedBuyNowSign(sbl, p.getId()));
        Bukkit.getScheduler().runTask(plugin, new BuyNowSignUpdater(plugin));
    }
}
