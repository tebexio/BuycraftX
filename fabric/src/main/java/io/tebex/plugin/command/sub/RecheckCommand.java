package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class RecheckCommand extends SubCommand {
    private final TebexPlugin platform;

    public RecheckCommand(TebexPlugin platform) {
        super(platform, "recheck", "tebex.admin");
        this.platform = platform;
    }

    @Override
    public void execute(CommandContext<ServerCommandSource> context) {
        if(! platform.isSetup()) {
            context.getSource().sendFeedback(new LiteralText("§cTebex is not setup yet!"), false);
            return;
        }

        // if running from console, return
        if (context.getSource().getEntity() != null) {
            context.getSource().sendFeedback(new LiteralText("§b[Tebex] §7Performing recheck.."), false);
        }

        getPlatform().performCheck(false);
    }

    @Override
    public String getDescription() {
        return "Rechecks for new purchases.";
    }
}
