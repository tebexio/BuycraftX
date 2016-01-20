package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class InformationSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.getApiClient() == null) {
            sender.sendMessage(ChatColor.RED + "Set up a secret key first with /buycraft secret.");
            return;
        }

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(ChatColor.RED + "No server information found.");
            return;
        }

        sender.sendMessage(ChatColor.GRAY + "Information on this server:");
        sender.sendMessage(String.format(ChatColor.GRAY + "Server " + ChatColor.GREEN + "%s" + ChatColor.GRAY + " for" +
                " webstore " + ChatColor.GREEN + "%s", plugin.getServerInformation().getServer().getName(),
                plugin.getServerInformation().getAccount().getName()));
        sender.sendMessage(ChatColor.GRAY + "Server prices are in " + ChatColor.GREEN +
                plugin.getServerInformation().getAccount().getCurrency().getIso4217());
        sender.sendMessage(ChatColor.GRAY + "Webstore domain: " +
                ChatColor.GREEN + plugin.getServerInformation().getAccount().getDomain());
    }

    @Override
    public String getDescription() {
        return "Retrieves public information about the webstore this server is associated with.";
    }
}
