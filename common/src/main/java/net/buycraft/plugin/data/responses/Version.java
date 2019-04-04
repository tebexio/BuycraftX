package net.buycraft.plugin.data.responses;

import java.util.Date;

public final class Version {
    private final String version;
    private final Date released;

    public Version(final String version, final Date released) {
        this.version = version;
        this.released = released;
    }

    public String getVersion() {
        return this.version;
    }

    public Date getReleased() {
        return this.released;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Version)) return false;
        final Version other = (Version) o;
        final java.lang.Object this$version = this.getVersion();
        final java.lang.Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final java.lang.Object this$released = this.getReleased();
        final java.lang.Object other$released = other.getReleased();
        if (this$released == null ? other$released != null : !this$released.equals(other$released)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final java.lang.Object $released = this.getReleased();
        result = result * PRIME + ($released == null ? 43 : $released.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "Version(version=" + this.getVersion() + ", released=" + this.getReleased() + ")";
    }
}
