package net.buycraft.plugin.data;

import com.google.api.client.json.JsonString;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Objects;

@Value
public class Package implements Comparable<Package> {
    private final int id;
    private final int order;
    private final String name;
    @JsonString
    private final BigDecimal price;

    @Override
    public int compareTo(Package o) {
        Objects.requireNonNull(o, "package");
        return Integer.compare(order, o.getOrder());
    }
}
