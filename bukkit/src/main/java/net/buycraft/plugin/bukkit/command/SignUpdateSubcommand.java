package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class SignUpdateSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_params"));
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("need_secret_key"));
            return;
        }

        if (plugin.getDuePlayerFetcher().inProgress()) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("already_checking_for_purchases"));
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new RecentPurchaseSignUpdateFetcher(plugin));
        sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("sign_update_queued"));
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_signupdate");
    }
}
