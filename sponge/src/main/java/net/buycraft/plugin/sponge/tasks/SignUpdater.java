package net.buycraft.plugin.sponge.tasks;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SignUpdater implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        List<RecentPurchaseSignPosition> signs = plugin.getRecentPurchaseSignStorage().getSigns();
        OptionalInt maxPos = signs.stream().mapToInt(RecentPurchaseSignPosition::getPosition).max();

        if (!maxPos.isPresent()) {
            // Nothing to do
            return;
        }

        if (plugin.getApiClient() == null) {
            // Can't use API client
            return;
        }

        List<RecentPayment> payments;
        try {
            payments = plugin.getApiClient().getRecentPayments(Math.min(100, maxPos.getAsInt()));
        } catch (IOException | ApiException e) {
            plugin.getLogger().error("Could not fetch recent purchases", e);
            return;
        }

        Map<RecentPurchaseSignPosition, RecentPayment> signToPurchases = new HashMap<>();
        for (RecentPurchaseSignPosition sign : signs) {
            if (sign.getPosition() > payments.size()) {
                signToPurchases.put(sign, null);
            } else {
                signToPurchases.put(sign, payments.get(sign.getPosition() - 1));
            }
        }

        // Now look up game profiles so that heads can be properly displayed.
        Set<String> usernames = payments.stream()
                .map(payment -> payment.getPlayer().getName())
                .collect(Collectors.toSet());
        // Add MHF_Question too.
        usernames.add("MHF_Question");
        CompletableFuture<Collection<GameProfile>> future = Sponge.getServer().getGameProfileManager().getAllByName(usernames, true);
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().error("Unable to fetch player profiles", throwable);
                return;
            }

            Map<String, GameProfile> profileMap = result.stream().filter(p -> p.getName().isPresent())
                    .collect(Collectors.toMap(p -> p.getName().get(), Function.identity()));

            Sponge.getScheduler().createTaskBuilder()
                    .execute(new SignUpdateApplication(plugin, signToPurchases, profileMap))
                    .submit(plugin);
        });
    }
}
