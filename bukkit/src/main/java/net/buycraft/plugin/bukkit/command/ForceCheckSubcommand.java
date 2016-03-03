package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class ForceCheckSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "This command does not accept any parameters.");
            return;
        }

        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + "Set up a secret key first with /buycraft secret.");
            return;
        }

        if (plugin.getDuePlayerFetcher().getInProgress().get()) {
            sender.sendMessage(ChatColor.RED + "We're currently checking for new purchases. Sit tight!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getDuePlayerFetcher().run(false);
            }
        });
        sender.sendMessage(ChatColor.GREEN + "Successfully queued player check.");
    }

    @Override
    public String getDescription() {
        return "Forces a purchase check.";
    }
}
