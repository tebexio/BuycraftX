package io.tebex.plugin.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.gui.BuyGUI;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class BuyCommand {
    private final TebexPlugin plugin;
    public BuyCommand(TebexPlugin plugin) {
        this.plugin = plugin;
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();

        try {
            ServerPlayerEntity player = source.getPlayer();
            new BuyGUI(plugin).open(player);
        } catch (CommandSyntaxException e) {
            source.sendFeedback(new LiteralText("§b[Tebex] §7You must be a player to run this command!"), false);
        }

        return 1;
    }
}
