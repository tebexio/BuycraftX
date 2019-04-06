package net.buycraft.plugin.data.responses;

import java.util.Date;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version1 = (Version) o;

        if (!Objects.equals(version, version1.version)) return false;
        return Objects.equals(released, version1.released);
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (released != null ? released.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Version(version=" + this.getVersion() + ", released=" + this.getReleased() + ")";
    }
}
