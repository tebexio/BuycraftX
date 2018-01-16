package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateFetcher;
import net.buycraft.plugin.bukkit.tasks.SendCheckoutLink;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SendLinkSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_permission"));
            return;
        }

        if (args.length != 3 || !(args[1].equalsIgnoreCase("package") || args[1].equalsIgnoreCase("category")) || !StringUtils.isNumeric(args[2])) {
            sender.sendMessage(ChatColor.RED + "Incorrect syntax: /buycraft sendlink <player> package|category <id>");
            return;
        }

        Player p = Bukkit.getPlayer(args[0]);

        if (p == null || !p.isOnline()) {
            sender.sendMessage(ChatColor.RED + "That player is not online!");
            return;
        }

        if (args[1].equalsIgnoreCase("package")) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, Integer.valueOf(args[2]), p, false, sender));
            return;
        } else if (args[1].equalsIgnoreCase("category")) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new SendCheckoutLink(plugin, Integer.valueOf(args[2]), p, true, sender));
            return;
        }
    }


    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_sendlink");
    }
}
