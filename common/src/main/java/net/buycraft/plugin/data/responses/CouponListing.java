package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.Coupon;

import java.util.List;

public final class CouponListing {
    private final List<Coupon> data;

    public CouponListing(final List<Coupon> data) {
        this.data = data;
    }

    public List<Coupon> getData() {
        return this.data;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof CouponListing)) return false;
        final CouponListing other = (CouponListing) o;
        final java.lang.Object this$data = this.getData();
        final java.lang.Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "CouponListing(data=" + this.getData() + ")";
    }
}
