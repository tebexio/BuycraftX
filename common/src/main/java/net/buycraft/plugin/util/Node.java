package net.buycraft.plugin.util;

import com.google.common.base.Optional;
import lombok.Getter;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;

import java.util.List;

/**
 * Created by meyerzinn on 2/17/16.
 * This was created with the intention of allowing traversal
 */

public class Node {

    @Getter
    private List<Category> subcategories;
    @Getter
    private List<Package> packages;
    @Getter
    private String title;
    @Getter
    private Optional<Node> parent;

    public Node(List<Category> subcategories, List<Package> packages, String title, Optional parent) {
        this.subcategories = subcategories;
        this.packages = packages;
        this.title = title;
        this.parent = parent;
    }

    public Node getChild(Category subcategory) {
        return new Node(subcategory.getSubcategories(), subcategory.getPackages(), subcategory.getName(), Optional.of(this));
    }

}