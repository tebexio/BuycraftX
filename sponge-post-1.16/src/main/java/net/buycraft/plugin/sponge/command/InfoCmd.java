package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.util.Color;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class InfoCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public InfoCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        Audience sender = (Audience) args.cause().root();

        if (plugin.getApiClient() == null) {
            sender.sendMessage(Component.text(plugin.getI18n().get("generic_api_operation_error")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        if (plugin.getServerInformation() == null) {
            sender.sendMessage(Component.text(plugin.getI18n().get("information_no_server")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        String webstoreURL = plugin.getServerInformation().getAccount().getDomain();
        try {
            Component webstore = Component.text(webstoreURL)
                    .color(TextColor.color(Color.GREEN))
                    .clickEvent(ClickEvent.openUrl(new URL(webstoreURL)))
                    .hoverEvent(HoverEvent.showText(Component.text(webstoreURL)));

            Component server = Component.text(plugin.getServerInformation().getServer().getName()).color(TextColor.color(Color.GREEN));

            Arrays.asList(
                    Component.text(plugin.getI18n().get("information_title") + " ").color(TextColor.color(Color.GRAY)),
                    Component.text(plugin.getI18n().get("information_sponge_server") + " ").color(TextColor.color(Color.GRAY)).append(server),
                    Component.text(plugin.getI18n().get("information_currency", plugin.getServerInformation().getAccount().getCurrency().getIso4217())).color(TextColor.color(Color.GRAY)),
                    Component.text(plugin.getI18n().get("information_domain", "")).color(TextColor.color(Color.GRAY)).append(webstore)
            ).forEach(sender::sendMessage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return CommandResult.success();
    }
}
