package net.buycraft.plugin.shared.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.Listing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListingUpdateTask implements Runnable {
    private final IBuycraftPlatform platform;
    private final AtomicReference<Listing> listing = new AtomicReference<>();
    private final AtomicReference<Date> lastUpdate = new AtomicReference<>();
    private final Runnable updateTask;

    @Override
    public void run() {
        if (platform.getApiClient() == null) {
            // no API client
            return;
        }

        try {
            listing.set(platform.getApiClient().retrieveListing());
        } catch (IOException | ApiException e) {
            platform.log(Level.SEVERE, "Error whilst retrieving listing", e);
            return;
        }

        lastUpdate.set(new Date());

        if (updateTask != null) {
            updateTask.run();
        }
    }

    public Listing getListing() {
        return listing.get();
    }

    public Date getLastUpdate() {
        return lastUpdate.get();
    }

    public Package getPackageById(int id) {
        List<Category> categories = getListing().getCategories();

        if(categories == null) {
            System.out.println("Could not get a list of categories when searching for package " + id + ".");
            return null;
        }

        for (Category category : categories) {
            Package p = doSearch(id, category);
            if (p != null)
                return p;
        }

        return null;
    }

    private Package doSearch(int id, Category category) {
        for (Package aPackage : category.getPackages()) {
            if (aPackage.getId() == id)
                return aPackage;
        }

        for (Category sub : category.getSubcategories()) {
            Package p = doSearch(id, sub);
            if (p != null)
                return p;
        }

        return null;
    }
}
