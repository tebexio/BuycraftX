package io.tebex.plugin.command.sub;

import com.velocitypowered.api.command.CommandSource;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import net.kyori.adventure.text.Component;

import java.io.IOException;

public class ReloadCommand extends SubCommand {
    public ReloadCommand(TebexPlugin platform) {
        super(platform, "reload", "tebex.admin");
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        TebexPlugin platform = getPlatform();
        try {
            YamlDocument configYaml = platform.initPlatformConfig();
            platform.loadServerPlatformConfig(configYaml);
            sender.sendMessage(Component.text("§8[Tebex] §7Successfully reloaded."));
        } catch (IOException e) {
            sender.sendMessage(Component.text("§8[Tebex] §cFailed to reload the plugin: Check Console."));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin.";
    }
}
