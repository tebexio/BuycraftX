package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public class RecentPurchaseSignUpdateFetcher implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        // Figure out how many signs we should get
        List<RecentPurchaseSignPosition> signs = plugin.getRecentPurchaseSignStorage().getSigns();

        int max = 0;
        for (RecentPurchaseSignPosition sign : signs) {
            if (sign.getPosition() > max) {
                max = sign.getPosition();
            }
        }

        if (max == 0) {
            return;
        }

        if (plugin.getApiClient() == null) {
            return;
        }

        List<RecentPayment> payments;
        try {
            payments = plugin.getApiClient().getRecentPayments(Math.min(100, max));
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not fetch recent purchases", e);
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

        plugin.getServer().getScheduler().runTask(plugin, new RecentPurchaseSignUpdateApplication(plugin, signToPurchases));
    }
}
