package net.buycraft.plugin.data;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Objects;

@Value
public class Package implements Comparable<Package> {
    private final int id;
    private final int order;
    private final String name;
    private final BigDecimal price;
    private final Sale sale;

    @Override
    public int compareTo(Package o) {
        Objects.requireNonNull(o, "package");
        return Integer.compare(order, o.getOrder());
    }

    public BigDecimal getEffectivePrice() {
        if (sale != null) {
            return price.setScale(2, BigDecimal.ROUND_HALF_UP).subtract(sale.getDiscount().setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Value
    public static class Sale {
        private final boolean active;
        private final BigDecimal discount;
    }
}
