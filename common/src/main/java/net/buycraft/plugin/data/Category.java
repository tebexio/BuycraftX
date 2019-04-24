package net.buycraft.plugin.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Category implements Comparable<Category> {
    private final int id;
    private final int order;
    private final String name;
    private final List<Package> packages;
    private final List<Category> subcategories;
    @SerializedName("gui_item")
    private final String guiItem;

    public Category(final int id, final int order, final String name, final List<Package> packages, final List<Category> subcategories, final String guiItem) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.packages = packages;
        this.subcategories = subcategories;
        this.guiItem = guiItem;
    }

    @Override
    public int compareTo(Category o) {
        Objects.requireNonNull(o, "category");
        return Integer.compare(order, o.getOrder());
    }

    public List<Category> getSubcategories() {
        return subcategories == null ? ImmutableList.of() : subcategories;
    }

    public void order() {
        packages.sort(Comparator.comparingInt(Package::getOrder));
        if (subcategories != null) {
            subcategories.sort(Comparator.comparingInt(Category::getOrder));
            for (Category category : subcategories) {
                category.order();
            }
        }
    }

    public int getId() {
        return this.id;
    }

    public int getOrder() {
        return this.order;
    }

    public String getName() {
        return this.name;
    }

    public List<Package> getPackages() {
        return this.packages;
    }

    public String getGuiItem() {
        return this.guiItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (id != category.id) return false;
        if (order != category.order) return false;
        if (!Objects.equals(name, category.name)) return false;
        if (!Objects.equals(packages, category.packages)) return false;
        if (!Objects.equals(subcategories, category.subcategories)) return false;
        return Objects.equals(guiItem, category.guiItem);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + order;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (packages != null ? packages.hashCode() : 0);
        result = 31 * result + (subcategories != null ? subcategories.hashCode() : 0);
        result = 31 * result + (guiItem != null ? guiItem.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Category(id=" + this.getId() + ", order=" + this.getOrder() + ", name=" + this.getName() + ", packages=" + this.getPackages() + ", subcategories=" + this.getSubcategories() + ", guiItem=" + this.getGuiItem() + ")";
    }
}
