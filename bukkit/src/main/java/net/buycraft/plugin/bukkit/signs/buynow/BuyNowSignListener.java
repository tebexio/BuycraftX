package net.buycraft.plugin.bukkit.signs.buynow;

import lombok.Getter;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateApplication;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.bukkit.util.BukkitSerializedBlockLocation;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.shared.config.signs.storage.SavedBuyNowSign;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyNowSignListener implements Listener {
    @Getter
    private final Map<UUID, SerializedBlockLocation> settingUpSigns = new HashMap<>();
    private final BuycraftPlugin plugin;
    private final Map<UUID, Long> signCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 250; // 5 ticks

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


        settingUpSigns.put(event.getPlayer().getUniqueId(), BukkitSerializedBlockLocation.create(event.getBlock().getLocation()));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Navigate to the item you want to set this sign for.");

        plugin.getViewCategoriesGUI().open(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block b = event.getClickedBlock();

            if (!(b.getType() == Material.WALL_SIGN || b.getType() == Material.LEGACY_SIGN_POST))
                return;

            SerializedBlockLocation sbl = BukkitSerializedBlockLocation.create(event.getClickedBlock().getLocation());

            for (SavedBuyNowSign s : plugin.getBuyNowSignStorage().getSigns()) {
                if (s.getLocation().equals(sbl)) {
                    // Signs are rate limited (per player) in order to limit API calls issued.
                    Long ts = signCooldowns.get(event.getPlayer().getUniqueId());
                    long now = System.currentTimeMillis();
                    if (ts == null || ts + COOLDOWN_MS <= now) {
                        signCooldowns.put(event.getPlayer().getUniqueId(), now);
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, s.getPackageId(),
                                event.getPlayer()));
                    }

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (settingUpSigns.containsKey(event.getPlayer().getUniqueId())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if ((event.getPlayer().getOpenInventory().getTopInventory() == null ||
                            !event.getPlayer().getOpenInventory().getTopInventory().getTitle().startsWith("Buycraft: ")) &&
                            settingUpSigns.remove(event.getPlayer().getUniqueId()) != null &&
                            event.getPlayer() instanceof Player) {
                        ((Player) event.getPlayer()).sendMessage(ChatColor.RED + "Buy sign set up cancelled.");
                    }
                }
            }, 3);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.LEGACY_SIGN_POST) {
            SerializedBlockLocation location = BukkitSerializedBlockLocation.create(event.getBlock().getLocation());
            if (plugin.getBuyNowSignStorage().containsLocation(location)) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }

                if (plugin.getBuyNowSignStorage().removeSign(location)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed buy now sign!");
                }
            }
            return;
        }

        for (BlockFace face : RecentPurchaseSignUpdateApplication.FACES) {
            Location onFace = event.getBlock().getRelative(face).getLocation();
            SerializedBlockLocation onFaceSbl = BukkitSerializedBlockLocation.create(onFace);

            if (plugin.getBuyNowSignStorage().containsLocation(onFaceSbl)) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }

                if (plugin.getBuyNowSignStorage().removeSign(onFaceSbl)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed buy now sign!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        settingUpSigns.remove(event.getPlayer().getUniqueId());
        signCooldowns.remove(event.getPlayer().getUniqueId());
    }

    public void doSignSetup(Player player, Package p) {
        SerializedBlockLocation sbl = settingUpSigns.remove(player.getUniqueId());
        if (sbl == null)
            return;

        Block b = BukkitSerializedBlockLocation.toBukkit(sbl).getBlock();

        if (!(b.getType() == Material.WALL_SIGN || b.getType() == Material.LEGACY_SIGN_POST))
            return;

        plugin.getBuyNowSignStorage().addSign(new SavedBuyNowSign(sbl, p.getId()));
        plugin.getServer().getScheduler().runTask(plugin, new BuyNowSignUpdater(plugin));
    }
}
