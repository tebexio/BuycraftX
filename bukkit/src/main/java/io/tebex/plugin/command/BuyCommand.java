package io.tebex.plugin.command;

import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.gui.BuyGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyCommand extends Command {
    private final TebexPlugin platform;

    public BuyCommand(String command, TebexPlugin platform) {
        super(command);
        this.platform = platform;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(! platform.isSetup()) {
            sender.sendMessage(ChatColor.RED + "Tebex is not setup yet!");
            return true;
        }

        platform.getBuyGUI().open((Player) sender);
        return true;
    }
}
