package net.buycraft.plugin.bukkit.signs.buynow;

import lombok.Getter;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;
import net.buycraft.plugin.data.Package;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class BuyNowSignListener implements Listener {
    @Getter
    private final Map<UUID, SerializedBlockLocation> settingUpSigns = new HashMap<>();
    private final BuycraftPlugin plugin;

    public BuyNowSignListener(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            Block b = event.getClickedBlock();

            if (b.getType() != Material.WALL_SIGN || b.getType() != Material.SIGN_POST)
                return;

            Sign sign = (Sign) b.getState();

            if (!sign.getLine(0).equals(ChatColor.BLUE + "[Buycraft]"))
                return;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, Integer.parseInt(sign.getLine(2)),
                    event.getPlayer()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (settingUpSigns.remove(event.getPlayer().getUniqueId()) != null) {
            event.getPlayer().sendMessage(ChatColor.RED + "Buy sign set up cancelled.");
        }
    }

    public void doSignSetup(Player player, Package p) {
        SerializedBlockLocation sbl = settingUpSigns.remove(player.getUniqueId());
        if (sbl == null)
            return;

        Block b = sbl.toBukkitLocation().getBlock();

        if (b.getType() != Material.WALL_SIGN || b.getType() != Material.SIGN_POST)
            return;

        Sign sign = (Sign) b.getState();
        sign.setLine(0, ChatColor.BLUE + "[Buycraft]");
        sign.setLine(1, StringUtils.abbreviate(p.getName(), 16));
        sign.setLine(2, Integer.toString(p.getId()));
        sign.setLine(3, "");
        sign.update();
    }
}
