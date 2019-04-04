package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.Coupon;

public final class CouponSingleListing {
    private final Coupon data;

    public CouponSingleListing(final Coupon data) {
        this.data = data;
    }

    public Coupon getData() {
        return this.data;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof CouponSingleListing)) return false;
        final CouponSingleListing other = (CouponSingleListing) o;
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
        return "CouponSingleListing(data=" + this.getData() + ")";
    }
}
