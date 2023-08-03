package io.tebex.plugin.command.sub;

import com.mojang.brigadier.context.CommandContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.sdk.SDK;
import io.tebex.sdk.exception.ServerNotFoundException;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class SecretCommand extends SubCommand {
    public SecretCommand(TebexPlugin platform) {
        super(platform, "secret", "tebex.setup");
    }

    @Override
    public void execute(CommandContext<ServerCommandSource> context) {
        final ServerCommandSource source = context.getSource();

        String serverToken = context.getArgument("key", String.class);
        TebexPlugin platform = getPlatform();

        if(platform.isSetup()) {
            source.sendFeedback(new LiteralText("§b[Tebex] §7Already connected to a store."), false);
            return;
        }

        SDK analyse = platform.getSDK();
        ServerPlatformConfig analyseConfig = platform.getPlatformConfig();
        YamlDocument configFile = analyseConfig.getYamlDocument();

        analyse.setSecretKey(serverToken);

        platform.getSDK().getServerInformation().thenAccept(serverInformation -> {
            analyseConfig.setSecretKey(serverToken);
            configFile.set("server.secret-key", serverToken);

            try {
                configFile.save();
            } catch (IOException e) {
                source.sendFeedback(new LiteralText("§b[Tebex] §7Failed to save config: " + e.getMessage()), false);
            }

            source.sendFeedback(new LiteralText("§b[Tebex] §7Connected to §b" + serverInformation.getServer().getName() + "§7."), false);
            platform.configure();
        }).exceptionally(ex -> {
            Throwable cause = ex.getCause();

            if(cause instanceof ServerNotFoundException) {
                source.sendFeedback(new LiteralText("§b[Tebex] §7Server not found. Please check your secret key."), false);
                platform.halt();
            } else {
                source.sendFeedback(new LiteralText("§b[Tebex] §cAn error occurred: " + cause.getMessage()), false);
                cause.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public String getDescription() {
        return "Connects to your Tebex store.";
    }

    @Override
    public String getUsage() {
        return "<key>";
    }
}
