package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.Category;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Listing {
    private final List<Category> categories;

    public Listing(final List<Category> categories) {
        this.categories = categories;
    }

    public void order() {
        Collections.sort(categories);
        for (Category category : categories) {
            category.order();
        }
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Listing listing = (Listing) o;

        return Objects.equals(categories, listing.categories);
    }

    @Override
    public int hashCode() {
        return categories != null ? categories.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Listing(categories=" + this.getCategories() + ")";
    }
}
