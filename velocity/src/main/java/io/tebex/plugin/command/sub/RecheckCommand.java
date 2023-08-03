package io.tebex.plugin.command.sub;

import com.velocitypowered.api.command.CommandSource;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.kyori.adventure.text.Component;

public class RecheckCommand extends SubCommand {
    private final TebexPlugin platform;

    public RecheckCommand(TebexPlugin platform) {
        super(platform, "recheck", "tebex.admin");
        this.platform = platform;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        if(! platform.isSetup()) {
            sender.sendMessage(Component.text("§cTebex is not setup yet!"));
            return;
        }

        sender.sendMessage(Component.text("§b[Tebex] §7Performing recheck.."));
        getPlatform().performCheck();
    }

    @Override
    public String getDescription() {
        return "Rechecks for new purchases.";
    }
}
