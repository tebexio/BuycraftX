package net.buycraft.plugin.data;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Value
public class Category implements Comparable<Category> {
    private final int id;
    private final int order;
    private final String name;
    private final List<Package> packages;
    private final List<Category> subcategories;
    private final String gui_item;

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
}
