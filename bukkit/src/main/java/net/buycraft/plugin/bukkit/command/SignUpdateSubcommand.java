package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.RecentPurchaseSignUpdateFetcher;
import net.buycraft.plugin.shared.IBuycraftPlugin;
import net.buycraft.plugin.shared.commands.BuycraftCommandSender;
import net.buycraft.plugin.shared.commands.BuycraftSubcommand;
import net.buycraft.plugin.shared.commands.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class SignUpdateSubcommand implements BuycraftSubcommand {
    @Override
    public void execute(IBuycraftPlugin plugin, BuycraftCommandSender player, String[] args) {
        if (args.length != 0) {
            player.sendMessage(ChatColor.RED, "no_params");
            return;
        }

        if (plugin.getPlatform().getApiClient() == null) {
            player.sendMessage(ChatColor.RED, "need_secret_key");
            return;
        }

        if (plugin.getDuePlayerFetcher().inProgress()) {
            player.sendMessage(ChatColor.RED, "already_checking_for_purchases");
            return;
        }

        BuycraftPlugin bukkitPlugin = (BuycraftPlugin) plugin;
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, new RecentPurchaseSignUpdateFetcher(bukkitPlugin));
        player.sendMessage(ChatColor.GREEN, "sign_update_queued");
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_signupdate";
    }
}
