package net.buycraft.plugin.velocity;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.buycraft.plugin.velocity.command.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuycraftCommand implements SimpleCommand {
    private final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
    private final BuycraftPlugin plugin;

    public BuycraftCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(Component.text(plugin.getI18n().get("no_permission")).color(NamedTextColor.RED));
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
        sender.sendMessage(Component.text(plugin.getI18n().get("usage")).color(NamedTextColor.DARK_AQUA).decoration(TextDecoration.BOLD, true));
        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            sender.sendMessage(Component.text("/tebex " + entry.getKey()).color(NamedTextColor.GREEN).append(Component.text(": " + entry.getValue().getDescription())));
        }
    }

    public Map<String, Subcommand> getSubcommandMap() {
        return this.subcommandMap;
    }
}
