package net.buycraft.plugin.velocity;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.velocity.command.Subcommand;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuycraftCommand implements Command {
    private final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
    private final BuycraftPlugin plugin;

    public BuycraftCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(TextComponent.of(plugin.getI18n().get("no_permission")).color(TextColor.RED));
            return;
        }

        if (args.length == 0) {
            showHelp(sender);
            return;
        }

        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(args[0])) {
                String[] withoutSubcommand = Arrays.copyOfRange(args, 1, args.length);
                entry.getValue().execute(sender, withoutSubcommand);
                return;
            }
        }

        showHelp(sender);
    }

    private void showHelp(CommandSource sender) {
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("usage")).color(TextColor.DARK_AQUA).decoration(TextDecoration.BOLD, true));
        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            sender.sendMessage(TextComponent.of("/tebex " + entry.getKey()).color(TextColor.GREEN).append(TextComponent.of(": " + entry.getValue().getDescription())));
        }
    }

    public Map<String, Subcommand> getSubcommandMap() {
        return this.subcommandMap;
    }
}
