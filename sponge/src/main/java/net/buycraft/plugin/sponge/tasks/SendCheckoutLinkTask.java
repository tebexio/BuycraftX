package net.buycraft.plugin.sponge.tasks;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by meyerzinn on 2/14/16.
 */
@RequiredArgsConstructor
public class SendCheckoutLinkTask implements Runnable {

    @NonNull
    private final BuycraftPlugin plugin;
    private final int pkgId;
    @NonNull
    private final Player player;

    @Override public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId);
        } catch (IOException | ApiException e) {
            player.sendMessage(
                    Text.builder("Whoops! We weren't able to get a link for you to check out this package.").color(TextColors.RED).build());
            return;
        }
        if (response != null) {
            player.sendMessage(Text.builder("                                            ").style(TextStyles.STRIKETHROUGH).build());
            try {
                player.sendMessage(Text.builder("To buy your package, click ").color(TextColors.GRAY).append(Text.builder("here").onClick(TextActions
                        .openUrl(new URL(response.getUrl()))).build()).append(Text.builder(".").color(TextColors.GRAY).build()).build());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            player.sendMessage(Text.builder("                                            ").style(TextStyles.STRIKETHROUGH).build());
        }
    }
}
