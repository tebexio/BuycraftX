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
            // no API client
            return;
        }

        try {
            listing.set(plugin.getApiClient().retrieveListing());
        } catch (IOException | ApiException e) {
            plugin.getLogger().error("Error whilst retrieving listing", e);
        }
        lastUpdate.set(new Date());
        plugin.getPlatform().executeBlocking(new GUIUpdateTask(plugin));
        plugin.getPlatform().executeBlocking(new BuyNowSignUpdater(plugin));
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

    public Category findCategory(Category root, int id) {
        for (Category a : root.getSubcategories()) {
            if (a.getId() == id) {
                return a;
            }
            if (a.getSubcategories() != null && !a.getSubcategories().isEmpty()) {
                Category b = findCategory(a, id);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }

    public Category findCategory(int id) {
        for (Category a : getListing().getCategories()) {
            if (a.getId() == id) {
                return a;
            }
            Category b = findCategory(a, id);
            if (b != null) {
                return b;
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
        if (category.getSubcategories() != null) {
            for (Category sub : category.getSubcategories()) {
                Package p = doSearch(id, sub);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }
}
