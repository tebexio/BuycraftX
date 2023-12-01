package io.tebex.plugin.command.sub;

import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RecheckCommand extends SubCommand {
    private final TebexPlugin platform;

    public RecheckCommand(TebexPlugin platform) {
        super(platform, "recheck", "tebex.admin");
        this.platform = platform;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(! platform.isSetup()) {
            sender.sendMessage(ChatColor.RED + "Tebex is not setup yet!");
            return;
        }

        sender.sendMessage("§b[Tebex] §7Performing recheck..");
        getPlatform().performCheck(false);
    }

    @Override
    public String getDescription() {
        return "Rechecks for new purchases.";
    }
}
