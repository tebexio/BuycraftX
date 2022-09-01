package net.buycraft.plugin.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.fabric.BuycraftPlugin;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.IOException;

public class TebexCommand {
    private final BuycraftPlugin plugin;

    public TebexCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("tebex").executes(context -> {
            onBaseCommand(context);
            return 1;
        }).then(CommandManager.literal("secret").then(CommandManager.argument("token", StringArgumentType.string()).executes(context -> {
            onSecretCommand(context);
            return 1;
        })))
        );
    }

    private void onBaseCommand(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Tebex - ").formatted(Formatting.AQUA).append(new LiteralText("TODO").formatted(Formatting.BOLD)), false);
    }

    private void onSecretCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if(source.getEntity() instanceof ServerPlayerEntity) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_console_only")), false);
            return;
        }

        String token = context.getArgument("token", String.class);

        plugin.getPlatform().executeAsync(() -> {
            String currentKey = plugin.getConfiguration().getServerKey();
            BuyCraftAPI client;
            try {
                client = BuyCraftAPI.create(token, plugin.getHttpClient());
                plugin.updateInformation(client);
            } catch (IOException e) {
                plugin.getLogger().error("Unable to verify secret", e);
                source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_does_not_work")).formatted(Formatting.RED), false);
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getListingUpdateTask().run();
            plugin.getConfiguration().setServerKey(token);
            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_cant_be_saved")).formatted(Formatting.RED), false);
            }
            source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_success",
                    information.getServer().getName(), information.getAccount().getName())).formatted(Formatting.GREEN), false);

            boolean repeatChecks = currentKey.equals("INVALID");

            plugin.getDuePlayerFetcher().run(repeatChecks);
        });
    }
}
