package net.buycraft.plugin.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.buycraft.plugin.fabric.BuycraftPlugin;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TebexCommand {
    private final BuycraftPlugin plugin;

    public TebexCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("tebex").executes(context -> {
            context.getSource().sendFeedback(Text.of("Test command"), false);
            return 1;
        }).then(CommandManager.literal("secret").then(CommandManager.argument("token", StringArgumentType.string()).executes(context -> {
            context.getSource().sendFeedback(Text.of("Setup as " + context.getArgument("token", String.class)), false);
            return 1;
        })))
        );
    }
}
