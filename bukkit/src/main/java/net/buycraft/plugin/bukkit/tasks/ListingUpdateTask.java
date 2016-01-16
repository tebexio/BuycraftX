package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.responses.Listing;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListingUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;
    private final AtomicReference<Listing> listing = new AtomicReference<>();

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            // no API client
            return;
        }

        try {
            listing.set(plugin.getApiClient().retrieveListing());
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Error whilst retrieving listing", e);
        }
    }

    public Listing getListing() {
        return listing.get();
    }
}
