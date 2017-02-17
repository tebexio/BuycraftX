package net.buycraft.plugin.data.responses;

import lombok.Value;
import net.buycraft.plugin.data.Coupon;

import java.util.List;

@Value
public class CouponListing {
    private final List<Coupon> data;
}
