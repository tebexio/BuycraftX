package net.buycraft.plugin.sponge.tasks;

import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Color;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class SendCheckoutLinkTask implements Runnable {
    @NotNull
    private final BuycraftPlugin plugin;
    private final int pkgId;
    @NotNull
    private final ServerPlayer player;

    public SendCheckoutLinkTask(@NotNull final BuycraftPlugin plugin, final int pkgId, @NotNull final ServerPlayer player) {
        this.plugin = Objects.requireNonNull(plugin);
        this.pkgId = pkgId;
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.name(), pkgId).execute().body();
        } catch (IOException e) {
            player.sendMessage(
                    Component.text(plugin.getI18n().get("cant_check_out")).color(TextColor.color(Color.RED)));
            return;
        }
        if (response != null) {
            player.sendMessage(Component.text("                                            ").style(Style.style(TextDecoration.UNDERLINED)));
            try {
                Arrays.asList(
                        Component.text(plugin.getI18n().get("to_buy_this_package")).color(TextColor.color(Color.GREEN)),
                        Component.text(response.getUrl()).color(TextColor.color(Color.BLUE)).style(Style.style(TextDecoration.UNDERLINED)).clickEvent(ClickEvent.openUrl(new URL(response.getUrl())))
                ).forEach(player::sendMessage);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            player.sendMessage(Component.text("                                            ").style(Style.style(TextDecoration.UNDERLINED)));
        }
    }
}
