package net.buycraft.plugin.shared.config.signs.storage;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedBuyNowSign that = (SavedBuyNowSign) o;

        if (packageId != that.packageId) return false;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + packageId;
        return result;
    }

    @Override
    public String toString() {
        return "SavedBuyNowSign(location=" + this.getLocation() + ", packageId=" + this.getPackageId() + ")";
    }
}
