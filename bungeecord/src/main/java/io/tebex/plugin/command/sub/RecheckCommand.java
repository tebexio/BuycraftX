package io.tebex.plugin.command.sub;

import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

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
        getPlatform().performCheck();
    }

    @Override
    public String getDescription() {
        return "Rechecks for new purchases.";
    }
}
