package net.buycraft.plugin.sponge.gui;

import lombok.Getter;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import org.spongepowered.api.text.translation.FixedTranslation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by meyerzinn on 2/17/16.
 * This is an object to represent a Node in a GuiView.
 * It comes with the basic assumption that it will always have Children and that any Child retrieved via {@link #getChild(Category)} with have its
 * parent field set to this node, and that any Node without a parent is a root node. This would allow the potential in the future for multiple root
 * nodes.
 */

public class Node {

    @Getter
    private List<Category> subcategories;
    @Getter
    private List<Package> packages;
    private String title;
    @Getter
    private Optional<Node> parent;

    public Node(List<Category> subcategories, List<Package> packages, String title, Optional<Node> parent) {
        this.subcategories = subcategories;
        this.packages = packages;
        this.title = title;
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public FixedTranslation getTranslatableTitle() {
        return new FixedTranslation(getTitle());
    }

    public Node getChild(Category subcategory) {
        return new Node(subcategory.getSubcategories(), subcategory.getPackages(), subcategory.getName(), Optional.of(this));
    }

}
