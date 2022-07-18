package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

public class SecretCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public SecretCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        if (!(src instanceof ConsoleSource)) {
            src.sendMessage(Text.builder(plugin.getI18n().get("secret_console_only")).color(TextColors.RED).build());
        } else {
            if (!args.getOne("secret").isPresent()) {
                src.sendMessage(Text.builder(plugin.getI18n().get("secret_need_key")).color(TextColors.RED).build());
            } else {
                plugin.getPlatform().executeAsync(() -> {
                    String currentKey = plugin.getConfiguration().getServerKey();
                    BuyCraftAPI client = BuyCraftAPI.create((String) args.getOne("secret").get(), plugin.getHttpClient());
                    try {
                        plugin.updateInformation(client);
                    } catch (IOException e) {
                        plugin.getLogger().error("Unable to verify secret", e);
                        src.sendMessage(Text.builder(plugin.getI18n().get("secret_does_not_work")).color(TextColors.RED).build());
                        return;
                    }

                    ServerInformation information = plugin.getServerInformation();
                    plugin.setApiClient(client);
                    plugin.getListingUpdateTask().run();
                    plugin.getConfiguration().setServerKey((String) args.getOne("secret").get());
                    try {
                        plugin.saveConfiguration();
                    } catch (IOException e) {
                        src.sendMessage(Text.builder(plugin.getI18n().get("secret_cant_be_saved")).color(TextColors.RED).build());
                    }
                    src.sendMessage(Text.builder(plugin.getI18n().get("secret_success",
                            information.getServer().getName(), information.getAccount().getName())).color(TextColors.GREEN).build());

                    boolean repeatChecks = false;
                    if (currentKey.equals("INVALID")) {
                        repeatChecks = true;
                    }

                    plugin.getDuePlayerFetcher().run(repeatChecks);
                });
            }
        }
        return CommandResult.success();
    }
}
