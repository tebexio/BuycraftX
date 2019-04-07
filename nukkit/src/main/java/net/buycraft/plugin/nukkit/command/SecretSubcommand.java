package net.buycraft.plugin.nukkit.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.nukkit.BuycraftPlugin;

import java.io.IOException;

public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public SecretSubcommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("secret_console_only"));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(TextFormat.RED + plugin.getI18n().get("secret_need_key"));
            return;
        }

        plugin.getPlatform().executeAsync(() -> {
            String currentKey = plugin.getConfiguration().getServerKey();
            BuyCraftAPI client = BuyCraftAPI.create(args[0], plugin.getHttpClient());
            try {
                plugin.updateInformation(client);
            } catch (IOException e) {
                plugin.getLogger().error("Unable to verify secret", e);
                sender.sendMessage(TextFormat.RED + plugin.getI18n().get("secret_does_not_work"));
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getConfiguration().setServerKey(args[0]);
            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                sender.sendMessage(TextFormat.RED + plugin.getI18n().get("secret_cant_be_saved"));
            }

            sender.sendMessage(TextFormat.GREEN + plugin.getI18n().get("secret_success",
                    information.getServer().getName(), information.getAccount().getName()));

            boolean repeatChecks = false;
            if (currentKey.equals("INVALID")) {
                repeatChecks = true;
            }

            plugin.getDuePlayerFetcher().run(repeatChecks);
        });
    }

    @Override
    public String getDescription() {
        return "Sets the secret key to use for this server.";
    }
}
