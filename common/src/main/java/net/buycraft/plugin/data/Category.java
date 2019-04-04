package net.buycraft.plugin.data;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Category implements Comparable<Category> {
    private final int id;
    private final int order;
    private final String name;
    private final List<Package> packages;
    private final List<Category> subcategories;
    private final String gui_item;

    public Category(final int id, final int order, final String name, final List<Package> packages, final List<Category> subcategories, final String gui_item) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.packages = packages;
        this.subcategories = subcategories;
        this.gui_item = gui_item;
    }

    @Override
    public int compareTo(Category o) {
        Objects.requireNonNull(o, "category");
        return Integer.compare(order, o.getOrder());
    }

    public List<Category> getSubcategories() {
        return subcategories == null ? ImmutableList.<Category>of() : subcategories;
    }

    public void order() {
        Collections.sort(packages);
        if (subcategories != null) {
            Collections.sort(subcategories);
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

    public String getGui_item() {
        return this.gui_item;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Category)) return false;
        final Category other = (Category) o;
        if (this.getId() != other.getId()) return false;
        if (this.getOrder() != other.getOrder()) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$packages = this.getPackages();
        final java.lang.Object other$packages = other.getPackages();
        if (this$packages == null ? other$packages != null : !this$packages.equals(other$packages)) return false;
        final java.lang.Object this$subcategories = this.getSubcategories();
        final java.lang.Object other$subcategories = other.getSubcategories();
        if (this$subcategories == null ? other$subcategories != null : !this$subcategories.equals(other$subcategories))
            return false;
        final java.lang.Object this$gui_item = this.getGui_item();
        final java.lang.Object other$gui_item = other.getGui_item();
        if (this$gui_item == null ? other$gui_item != null : !this$gui_item.equals(other$gui_item)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        result = result * PRIME + this.getOrder();
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $packages = this.getPackages();
        result = result * PRIME + ($packages == null ? 43 : $packages.hashCode());
        final java.lang.Object $subcategories = this.getSubcategories();
        result = result * PRIME + ($subcategories == null ? 43 : $subcategories.hashCode());
        final java.lang.Object $gui_item = this.getGui_item();
        result = result * PRIME + ($gui_item == null ? 43 : $gui_item.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "Category(id=" + this.getId() + ", order=" + this.getOrder() + ", name=" + this.getName() + ", packages=" + this.getPackages() + ", subcategories=" + this.getSubcategories() + ", gui_item=" + this.getGui_item() + ")";
    }
}
