package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.Coupon;

import java.util.Objects;

public final class CouponSingleListing {
    private final Coupon data;

    public CouponSingleListing(final Coupon data) {
        this.data = data;
    }

    public Coupon getData() {
        return this.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CouponSingleListing that = (CouponSingleListing) o;

        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CouponSingleListing(data=" + this.getData() + ")";
    }
}
