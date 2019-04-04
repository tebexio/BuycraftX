package net.buycraft.plugin.shared.config.signs.storage;

public final class SerializedBlockLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public SerializedBlockLocation(final String world, final int x, final int y, final int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof SerializedBlockLocation)) return false;
        final SerializedBlockLocation other = (SerializedBlockLocation) o;
        final java.lang.Object this$world = this.getWorld();
        final java.lang.Object other$world = other.getWorld();
        if (this$world == null ? other$world != null : !this$world.equals(other$world)) return false;
        if (this.getX() != other.getX()) return false;
        if (this.getY() != other.getY()) return false;
        if (this.getZ() != other.getZ()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $world = this.getWorld();
        result = result * PRIME + ($world == null ? 43 : $world.hashCode());
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getZ();
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "SerializedBlockLocation(world=" + this.getWorld() + ", x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
    }
}
