package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.GiftCard;

import java.util.List;
import java.util.Objects;

public final class GiftCardListing {
    private final List<GiftCard> data;

    public GiftCardListing(final List<GiftCard> data) {
        this.data = data;
    }

    public List<GiftCard> getData() {
        return this.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GiftCardListing that = (GiftCardListing) o;

        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GiftCardListing(data=" + this.getData() + ")";
    }
}
