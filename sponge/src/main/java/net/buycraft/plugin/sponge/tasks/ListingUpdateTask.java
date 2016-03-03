package net.buycraft.plugin.sponge.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.Listing;
import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class ListingUpdateTask implements Runnable {
    private final BuycraftPlugin plugin;
    private final AtomicReference<Listing> listing = new AtomicReference<>();
    private final AtomicReference<Date> lastUpdate = new AtomicReference<>();

    @Override
    public void run() {
        if (plugin.getApiClient() == null) {
            return;
        }

        try {
            listing.set(plugin.getApiClient().retrieveListing());
        } catch (IOException | ApiException e) {
            plugin.getLogger().error("Error whilst retrieving listing", e);
        }

        lastUpdate.set(new Date());

//        Bukkit.getScheduler().runTask(plugin, new GUIUpdateTask(plugin));
//        Bukkit.getScheduler().runTask(plugin, new BuyNowSignUpdater(plugin));
    }

    public Listing getListing() {
        return listing.get();
    }

    public Date getLastUpdate() {
        return lastUpdate.get();
    }

    public Package getPackageById(int id) {
        for (Category category : getListing().getCategories()) {
            Package p = doSearch(id, category);
            if (p != null) {
                return p;
            }
        }

        return null;
    }

    private Package doSearch(int id, Category category) {
        for (Package aPackage : category.getPackages()) {
            if (aPackage.getId() == id) {
                return aPackage;
            }
        }

        for (Category sub : category.getSubcategories()) {
            Package p = doSearch(id, sub);
            if (p != null) {
                return p;
            }
        }

        return null;
    }
}
