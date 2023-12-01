package io.tebex.plugin.command.sub;

import dev.dejvokep.boostedyaml.YamlDocument;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.gui.BuyGUI;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ReloadCommand extends SubCommand {
    public ReloadCommand(TebexPlugin platform) {
        super(platform, "reload", "tebex.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TebexPlugin platform = getPlatform();
        try {
            YamlDocument configYaml = platform.initPlatformConfig();
            platform.loadServerPlatformConfig(configYaml);
            platform.reloadConfig();
            platform.setBuyGUI(new BuyGUI(platform));

            sender.sendMessage("§8[Tebex] §7Successfully reloaded.");
        } catch (IOException e) {
            sender.sendMessage("§8[Tebex] §cFailed to reload the plugin: Check Console.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin.";
    }
}
