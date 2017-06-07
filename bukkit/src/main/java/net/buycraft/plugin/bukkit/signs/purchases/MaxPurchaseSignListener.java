package net.buycraft.plugin.bukkit.signs.purchases;

import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.MaxPurchaseSignUpdateFetcher;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateApplication;
import net.buycraft.plugin.bukkit.util.BukkitSerializedBlockLocation;
import net.buycraft.plugin.shared.config.signs.storage.MaxPurchaseSignPosition;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import org.apache.commons.lang.StringUtils;
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
public class MaxPurchaseSignListener implements Listener {
    private final BuycraftPlugin plugin;

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        boolean ourSign;
        try {
            ourSign = event.getLine(0).equalsIgnoreCase("[buycraft_mp]");
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
            pos = Integer.parseInt(StringUtils.trimToEmpty(event.getLine(1)));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "The second line must be a number.");
            return;
        }

        if (pos <= 0) {
            event.getPlayer().sendMessage(ChatColor.RED + "The second line can not be negative or zero.");
            return;
        }

        if (pos > 100) {
            event.getPlayer().sendMessage(ChatColor.RED + "No more than the 100 most recent purchases can be displayed on signs.");
            return;
        }
        
        int time = string2Time(StringUtils.trimToEmpty(event.getLine(2)));
        if(time <= 0) {
            event.getPlayer().sendMessage(ChatColor.RED + "The time is invalid.");
            return;
        }

        plugin.getMaxPurchaseSignStorage().addSign(new MaxPurchaseSignPosition(BukkitSerializedBlockLocation.create(
                event.getBlock().getLocation()), time, pos));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Added new max purchase sign!");

        for (int i = 0; i < 4; i++) {
            event.setLine(i, "");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new MaxPurchaseSignUpdateFetcher(plugin));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            SerializedBlockLocation location = BukkitSerializedBlockLocation.create(event.getBlock().getLocation());

            if (plugin.getMaxPurchaseSignStorage().containsLocation(location)) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }
                if (plugin.getMaxPurchaseSignStorage().removeSign(location)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed max purchase sign!");
                }
            }
            return;
        }

        for (BlockFace face : RecentPurchaseSignUpdateApplication.FACES) {
            Location onFace = event.getBlock().getRelative(face).getLocation();
            SerializedBlockLocation onFaceSbl = BukkitSerializedBlockLocation.create(onFace);
            if (plugin.getMaxPurchaseSignStorage().containsLocation(onFaceSbl)) {
                if (!event.getPlayer().hasPermission("buycraft.admin")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to break this sign.");
                    event.setCancelled(true);
                    return;
                }
                if (plugin.getMaxPurchaseSignStorage().removeSign(onFaceSbl)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Removed max purchase sign!");
                }
            }
        }
    }
    
    
    private int string2Time(String str) {
        String lettre = str.substring(str.length() - 1 );
        int chiffre;
        try {
            chiffre = Integer.parseInt(str.substring(0, str.length() - 1));
        } catch(NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "Not a number: {0} ({1})", 
                    new Object[]{str, e.getMessage()});
            return -1;
        }
        
        int resultat = 0;
        
        switch(lettre) {
            case "d":
                resultat = 1*chiffre;
                break;
                
            case "m":
                resultat = 31*chiffre;
                break;
                
            default:
                resultat = -1;
                break;
        }
        
        return resultat;
    }
    
    
}
