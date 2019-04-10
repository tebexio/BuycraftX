package net.buycraft.plugin.data;

import java.math.BigDecimal;
import java.util.Objects;

public final class Package implements Comparable<Package> {
    private final int id;
    private final int order;
    private final String name;
    private final BigDecimal price;
    private final Sale sale;
    private final String gui_item;

    public Package(final int id, final int order, final String name, final BigDecimal price, final Sale sale, final String gui_item) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.price = price;
        this.sale = sale;
        this.gui_item = gui_item;
    }

    @Override
    public int compareTo(Package o) {
        Objects.requireNonNull(o, "package");
        return Integer.compare(order, o.getOrder());
    }

    public BigDecimal getEffectivePrice() {
        BigDecimal rounded = price.setScale(2, BigDecimal.ROUND_HALF_UP);
        if (sale == null) {
            return rounded;
        }
        return rounded.subtract(sale.getDiscount().setScale(2, BigDecimal.ROUND_HALF_UP));
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

    public BigDecimal getPrice() {
        return this.price;
    }

    public Sale getSale() {
        return this.sale;
    }

    public String getGuiItem() {
        return this.gui_item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package) o;

        if (id != aPackage.id) return false;
        if (order != aPackage.order) return false;
        if (!Objects.equals(name, aPackage.name)) return false;
        if (!Objects.equals(price, aPackage.price)) return false;
        if (!Objects.equals(sale, aPackage.sale)) return false;
        return Objects.equals(gui_item, aPackage.gui_item);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + order;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (sale != null ? sale.hashCode() : 0);
        result = 31 * result + (gui_item != null ? gui_item.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Package(id=" + this.getId() + ", order=" + this.getOrder() + ", name=" + this.getName() + ", price=" + this.getPrice() + ", sale=" + this.getSale() + ", gui_item=" + this.getGuiItem() + ")";
    }

    public static final class Sale {
        private final boolean active;
        private final BigDecimal discount;

        public Sale(final boolean active, final BigDecimal discount) {
            this.active = active;
            this.discount = discount;
        }

        public boolean isActive() {
            return this.active;
        }

        public BigDecimal getDiscount() {
            return this.discount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Sale sale = (Sale) o;

            if (active != sale.active) return false;
            return Objects.equals(discount, sale.discount);
        }

        @Override
        public int hashCode() {
            int result = (active ? 1 : 0);
            result = 31 * result + (discount != null ? discount.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Package.Sale(active=" + this.isActive() + ", discount=" + this.getDiscount() + ")";
        }
    }
}
