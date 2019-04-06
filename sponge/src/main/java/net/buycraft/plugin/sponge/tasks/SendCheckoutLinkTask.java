package net.buycraft.plugin.sponge.tasks;

import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class SendCheckoutLinkTask implements Runnable {
    @NotNull
    private final BuycraftPlugin plugin;
    private final int pkgId;
    @NotNull
    private final Player player;

    public SendCheckoutLinkTask(@NotNull final BuycraftPlugin plugin, final int pkgId, @NotNull final Player player) {
        if (plugin == null) {
            throw new NullPointerException("plugin is marked @NotNull but is null");
        }
        if (player == null) {
            throw new NullPointerException("player is marked @NotNull but is null");
        }
        this.plugin = plugin;
        this.pkgId = pkgId;
        this.player = player;
    }

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId).execute().body();
        } catch (IOException e) {
            player.sendMessage(
                    Text.builder(plugin.getI18n().get("cant_check_out")).color(TextColors.RED).build());
            return;
        }
        if (response != null) {
            player.sendMessage(Text.builder("                                            ").style(TextStyles.STRIKETHROUGH).build());
            try {
                player.sendMessages(
                        Text.builder(plugin.getI18n().get("to_buy_this_package")).color(TextColors.GREEN).build(),
                        Text.builder(response.getUrl()).color(TextColors.BLUE).style(TextStyles.UNDERLINE)
                                .onClick(TextActions.openUrl(new URL(response.getUrl()))).build());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            player.sendMessage(Text.builder("                                            ").style(TextStyles.STRIKETHROUGH).build());
        }
    }
}
