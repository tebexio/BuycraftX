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

    public String getGui_item() {
        return this.gui_item;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Package)) return false;
        final Package other = (Package) o;
        if (this.getId() != other.getId()) return false;
        if (this.getOrder() != other.getOrder()) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$price = this.getPrice();
        final java.lang.Object other$price = other.getPrice();
        if (this$price == null ? other$price != null : !this$price.equals(other$price)) return false;
        final java.lang.Object this$sale = this.getSale();
        final java.lang.Object other$sale = other.getSale();
        if (this$sale == null ? other$sale != null : !this$sale.equals(other$sale)) return false;
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
        final java.lang.Object $price = this.getPrice();
        result = result * PRIME + ($price == null ? 43 : $price.hashCode());
        final java.lang.Object $sale = this.getSale();
        result = result * PRIME + ($sale == null ? 43 : $sale.hashCode());
        final java.lang.Object $gui_item = this.getGui_item();
        result = result * PRIME + ($gui_item == null ? 43 : $gui_item.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "Package(id=" + this.getId() + ", order=" + this.getOrder() + ", name=" + this.getName() + ", price=" + this.getPrice() + ", sale=" + this.getSale() + ", gui_item=" + this.getGui_item() + ")";
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
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Package.Sale)) return false;
            final Package.Sale other = (Package.Sale) o;
            if (this.isActive() != other.isActive()) return false;
            final java.lang.Object this$discount = this.getDiscount();
            final java.lang.Object other$discount = other.getDiscount();
            if (this$discount == null ? other$discount != null : !this$discount.equals(other$discount)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isActive() ? 79 : 97);
            final java.lang.Object $discount = this.getDiscount();
            result = result * PRIME + ($discount == null ? 43 : $discount.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "Package.Sale(active=" + this.isActive() + ", discount=" + this.getDiscount() + ")";
        }
    }
}
