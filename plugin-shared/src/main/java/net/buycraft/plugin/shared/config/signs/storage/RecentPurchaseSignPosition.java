package net.buycraft.plugin.shared.config.signs.storage;

import java.util.Objects;

public final class RecentPurchaseSignPosition {
    private final SerializedBlockLocation location;
    private final int position;

    public RecentPurchaseSignPosition(final SerializedBlockLocation location, final int position) {
        this.location = location;
        this.position = position;
    }

    public SerializedBlockLocation getLocation() {
        return this.location;
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecentPurchaseSignPosition that = (RecentPurchaseSignPosition) o;

        if (position != that.position) return false;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString() {
        return "RecentPurchaseSignPosition(location=" + this.getLocation() + ", position=" + this.getPosition() + ")";
    }
}
