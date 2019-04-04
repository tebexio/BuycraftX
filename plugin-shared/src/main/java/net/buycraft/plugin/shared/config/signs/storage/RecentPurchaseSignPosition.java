package net.buycraft.plugin.shared.config.signs.storage;

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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof RecentPurchaseSignPosition)) return false;
        final RecentPurchaseSignPosition other = (RecentPurchaseSignPosition) o;
        final java.lang.Object this$location = this.getLocation();
        final java.lang.Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
        if (this.getPosition() != other.getPosition()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        result = result * PRIME + this.getPosition();
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "RecentPurchaseSignPosition(location=" + this.getLocation() + ", position=" + this.getPosition() + ")";
    }
}
