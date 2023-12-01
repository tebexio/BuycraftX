package io.tebex.sdk.obj;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class SubCategory implements ICategory {
    private final int id;
    private final int order;
    private final String name;
    private final String guiItem;
    private final Category parentCategory;
    private final List<CategoryPackage> categoryPackages;

    public SubCategory(int id, int order, String name, String guiItem, Category parentCategory, List<CategoryPackage> categoryPackages) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.guiItem = guiItem;
        this.parentCategory = parentCategory;
        this.categoryPackages = categoryPackages;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGuiItem() {
        return guiItem;
    }

    public Category getParent() {
        return parentCategory;
    }

    public List<CategoryPackage> getPackages() {
        return categoryPackages;
    }

    public static SubCategory fromJsonObject(JsonObject jsonObject, Category category) {
        return new SubCategory(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("order").getAsInt(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("gui_item").getAsString(),
                category,
                jsonObject.getAsJsonArray("packages").asList().stream().map(item -> CategoryPackage.fromJsonObject(item.getAsJsonObject())).collect(Collectors.toList())
        );
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", order=" + order +
                ", name='" + name + '\'' +
                ", packages=" + categoryPackages +
                '}';
    }
}
