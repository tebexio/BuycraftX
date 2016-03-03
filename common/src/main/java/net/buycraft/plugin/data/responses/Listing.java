package net.buycraft.plugin.data.responses;

import lombok.Value;
import net.buycraft.plugin.data.Category;

import java.util.Collections;
import java.util.List;

@Value
public class Listing {
    private final List<Category> categories;

    public void order() {
        Collections.sort(categories);
        for (Category category : categories) {
            category.order();
        }
    }
}
