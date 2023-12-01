package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.manager.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Comparator;

public class HelpCommand extends SubCommand {
    private final CommandManager commandManager;
    public HelpCommand(TebexPlugin platform, CommandManager commandManager) {
        super(platform, "help", "tebex.admin");
        this.commandManager = commandManager;
    }

    @Override
    public void execute(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();

        source.sendFeedback(new LiteralText("§b[Tebex] §7Plugin Commands:"), false);

         commandManager
                 .getCommands()
                 .stream()
                 .sorted(Comparator.comparing(SubCommand::getName))
                 .forEach(subCommand -> source.sendFeedback(new LiteralText(" §8- §f/tebex " + subCommand.getName() + "§f" + (!subCommand.getUsage().isBlank() ? " §3" + subCommand.getUsage() + " " : " ") + "§7§o(" + subCommand.getDescription() + ")"), false));
    }

    @Override
    public String getDescription() {
        return "Shows this help page.";
    }
}
