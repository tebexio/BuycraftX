package net.buycraft.plugin.shared.util;

import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;

import java.util.List;
import java.util.Optional;

/**
 * Created by meyerzinn on 2/17/16.
 * <p>
 * This was created with the intention of allowing traversal
 */
public class Node {
    private List<Category> subcategories;
    private List<Package> packages;
    private String title;
    private Node parent;

    public Node(List<Category> subcategories, List<Package> packages, String title, Node parent) {
        this.subcategories = subcategories;
        this.packages = packages;
        this.title = title;
        this.parent = parent;
    }

    public Node getChild(Category subcategory) {
        return new Node(subcategory.getSubcategories(), subcategory.getPackages(), subcategory.getName(), this);
    }

    public Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    public List<Category> getSubcategories() {
        return this.subcategories;
    }

    public List<Package> getPackages() {
        return this.packages;
    }

    public String getTitle() {
        return this.title;
    }
}
