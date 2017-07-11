package net.buycraft.plugin.shared.tasks;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.Listing;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@RequiredArgsConstructor
public class CouponUpdateTask implements Runnable {
    private final IBuycraftPlatform platform;
    private final AtomicReference<List<Coupon>> listing = new AtomicReference<>();
    private final AtomicReference<Date> lastUpdate = new AtomicReference<>();
    private final Runnable updateTask;
    private final boolean verbose;

    @Override
    public void run() {

        if (verbose) {
            platform.log(Level.INFO, "Updating coupon list...");
        }

        if (platform.getApiClient() == null) {
            // no API client
            return;
        }

        try {
            listing.set(platform.getApiClient().getAllCoupons());
        } catch (IOException | ApiException e) {
            platform.log(Level.SEVERE, "Error whilst retrieving coupon listing", e);
            return;
        }

        lastUpdate.set(new Date());

        if (updateTask != null) {
            updateTask.run();
        }
    }

    public List<Coupon> getListing() {
        //Update the list
        run();
        List<Coupon> coupons = listing.get();
        return coupons == null ? ImmutableList.<Coupon>of() : coupons;
    }

    public Date getLastUpdate() {
        return lastUpdate.get();
    }

    public Coupon getCouponByCode(String code) {
        List<Coupon> listing = getListing();
        for (Coupon coupon : listing) {
            if (coupon.getCode().equalsIgnoreCase(code)) {
                return coupon;
            }
        }
        return null;
    }


}
