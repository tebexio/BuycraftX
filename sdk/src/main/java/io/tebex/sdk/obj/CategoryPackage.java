package io.tebex.sdk.obj;

import com.google.gson.JsonObject;

public class CategoryPackage {
    private final int id;
    private final int order;
    private final String name;
    private final double price;
    private final String image;
    private final String itemId;
    private final Sale sale;

    public CategoryPackage(int id, int order, String name, double price, String image, String itemId, Sale sale) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.price = price;
        this.image = image;
        this.itemId = itemId;
        this.sale = sale;
    }

    public int getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getGuiItem() {
        return itemId;
    }

    public Sale getSale() {
        return sale;
    }

    public boolean hasSale() {
        return sale.isActive();
    }

    public static class Sale {
        private final boolean active;
        private final double discount;

        public Sale(boolean active, double discount) {
            this.active = active;
            this.discount = discount;
        }

        public boolean isActive() {
            return active;
        }

        public double getDiscount() {
            return discount;
        }

        @Override
        public String toString() {
            return "Sale{" +
                    "active=" + active +
                    ", discount=" + discount +
                    '}';
        }
    }

    public static CategoryPackage fromJsonObject(JsonObject jsonObject) {
        JsonObject sale = jsonObject.getAsJsonObject("sale");

        return new CategoryPackage(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("order").getAsInt(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("price").getAsDouble(),
                jsonObject.get("image").getAsString(),
                jsonObject.get("gui_item").getAsString(),
                new Sale(sale.get("active").getAsBoolean(), sale.get("discount").getAsDouble())
        );
    }

    @Override
    public String toString() {
        return "Package{" +
                "id=" + id +
                ", order=" + order +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", itemId=" + itemId +
                ", sale=" + sale +
                '}';
    }
}
