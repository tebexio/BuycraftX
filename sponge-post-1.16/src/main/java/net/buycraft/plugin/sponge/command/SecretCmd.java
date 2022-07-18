package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.util.Color;

import java.io.IOException;
import java.util.Optional;

public class SecretCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public SecretCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(final CommandContext args) throws CommandException {
        final Audience src = (Audience) args.cause().root();
        
        if (!(src instanceof SystemSubject)) {
            src.sendMessage(Component.text(plugin.getI18n().get("secret_console_only")).color(TextColor.color(Color.RED)));
        } else {
            Optional<String> secretArg = args.one(Parameter.string().key("secret").build());
            if (!secretArg.isPresent()) {
                src.sendMessage(Component.text(plugin.getI18n().get("secret_need_key")).color(TextColor.color(Color.RED)));
            } else {
                plugin.getPlatform().executeAsync(() -> {
                    String currentKey = plugin.getConfiguration().getServerKey();
                    BuyCraftAPI client = BuyCraftAPI.create(secretArg.get(), plugin.getHttpClient());
                    try {
                        plugin.updateInformation(client);
                    } catch (IOException e) {
                        plugin.getLogger().error("Unable to verify secret", e);
                        src.sendMessage(Component.text(plugin.getI18n().get("secret_does_not_work")).color(TextColor.color(Color.RED)));
                        return;
                    }

                    ServerInformation information = plugin.getServerInformation();
                    plugin.setApiClient(client);
                    plugin.getListingUpdateTask().run();
                    plugin.getConfiguration().setServerKey(secretArg.get());
                    try {
                        plugin.saveConfiguration();
                    } catch (IOException e) {
                        src.sendMessage(Component.text(plugin.getI18n().get("secret_cant_be_saved")).color(TextColor.color(Color.RED)));
                    }
                    src.sendMessage(Component.text(plugin.getI18n().get("secret_success", information.getServer().getName(), information.getAccount().getName())).color(TextColor.color(Color.GREEN)));

                    boolean repeatChecks = currentKey.equals("INVALID");
                    plugin.getDuePlayerFetcher().run(repeatChecks);
                });
            }
        }
        return CommandResult.success();
    }
}
