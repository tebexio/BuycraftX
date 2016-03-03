package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SignUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class SignUpdateSubcommand implements Subcommand {
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new SignUpdater(plugin));
        sender.sendMessage(ChatColor.GREEN + "Successfully queued sign update.");
    }

    @Override
    public String getDescription() {
        return "Forces an update to your recent purchase signs.";
    }
}
