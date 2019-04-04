package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.Category;

import java.util.Collections;
import java.util.List;

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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Listing)) return false;
        final Listing other = (Listing) o;
        final java.lang.Object this$categories = this.getCategories();
        final java.lang.Object other$categories = other.getCategories();
        if (this$categories == null ? other$categories != null : !this$categories.equals(other$categories))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $categories = this.getCategories();
        result = result * PRIME + ($categories == null ? 43 : $categories.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "Listing(categories=" + this.getCategories() + ")";
    }
}
