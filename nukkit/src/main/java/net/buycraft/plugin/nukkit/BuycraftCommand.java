package net.buycraft.plugin.nukkit;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import net.buycraft.plugin.nukkit.command.Subcommand;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuycraftCommand implements CommandExecutor {
    private final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
    private final BuycraftPlugin plugin;

    public BuycraftCommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(TextFormat.DARK_AQUA + TextFormat.BOLD.toString() + plugin.getI18n().get("usage"));
        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            sender.sendMessage(TextFormat.GREEN + "/tebex " + entry.getKey() + TextFormat.GRAY + ": " + entry.getValue().getDescription());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("buycraft.admin")) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("no_permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(args[0])) {
                String[] withoutSubcommand = Arrays.copyOfRange(args, 1, args.length);
                entry.getValue().execute(sender, withoutSubcommand);
                return true;
            }
        }

        showHelp(sender);
        return true;
    }

    public Map<String, Subcommand> getSubcommandMap() {
        return this.subcommandMap;
    }
}
