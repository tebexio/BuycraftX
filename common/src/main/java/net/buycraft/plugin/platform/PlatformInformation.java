package net.buycraft.plugin.platform;

import org.jetbrains.annotations.NotNull;

public final class PlatformInformation {
    @NotNull
    private final PlatformType type;
    @NotNull
    private final String version;

    public PlatformInformation(@NotNull final PlatformType type, @NotNull final String version) {
        if (type == null) {
            throw new java.lang.NullPointerException("type is marked @NotNull but is null");
        }
        if (version == null) {
            throw new java.lang.NullPointerException("version is marked @NotNull but is null");
        }
        this.type = type;
        this.version = version;
    }

    @NotNull
    public PlatformType getType() {
        return this.type;
    }

    @NotNull
    public String getVersion() {
        return this.version;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PlatformInformation)) return false;
        final PlatformInformation other = (PlatformInformation) o;
        final java.lang.Object this$type = this.getType();
        final java.lang.Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final java.lang.Object this$version = this.getVersion();
        final java.lang.Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final java.lang.Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "PlatformInformation(type=" + this.getType() + ", version=" + this.getVersion() + ")";
    }
}
