package net.buycraft.plugin.sponge.tasks;

import com.sun.istack.internal.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;

/**
 * Created by meyerzinn on 2/14/16.
 */
@RequiredArgsConstructor
public class SendCheckoutLinkTask implements Runnable {

    @NotNull
    private final BuycraftPlugin plugin;
    @NonNull
    private final int pkgId;
    @NonNull
    private final Player player;

    @Override public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
