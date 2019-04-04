package net.buycraft.plugin.shared.config.signs.storage;

public final class SavedBuyNowSign {
    private final SerializedBlockLocation location;
    private final int packageId;

    public SavedBuyNowSign(final SerializedBlockLocation location, final int packageId) {
        this.location = location;
        this.packageId = packageId;
    }

    public SerializedBlockLocation getLocation() {
        return this.location;
    }

    public int getPackageId() {
        return this.packageId;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof SavedBuyNowSign)) return false;
        final SavedBuyNowSign other = (SavedBuyNowSign) o;
        final java.lang.Object this$location = this.getLocation();
        final java.lang.Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        if (this.getPackageId() != other.getPackageId()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        result = result * PRIME + this.getPackageId();
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "SavedBuyNowSign(location=" + this.getLocation() + ", packageId=" + this.getPackageId() + ")";
    }
}
