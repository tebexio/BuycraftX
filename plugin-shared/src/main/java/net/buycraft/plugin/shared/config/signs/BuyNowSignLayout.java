package net.buycraft.plugin.shared.config.signs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.data.Package;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class BuyNowSignLayout {
    public static final BuyNowSignLayout DEFAULT = new BuyNowSignLayout(ImmutableList.of(
            "&9[Package]",
            "%name%",
            "%price%"
    ));
    private final List<String> lines;

    public BuyNowSignLayout(List<String> lines) {
        this.lines = ImmutableList.copyOf(lines);
    }

    public List<String> format(Currency currency, Package p) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setCurrency(currency);

        List<String> formatted = new ArrayList<>();
        for (String line : lines) {
            formatted.add(line.replace("%name%", abbreviate(p.getName(), 16))
                    .replace("%price%", format.format(p.getEffectivePrice())));
        }
        return formatted;
    }

    private static String abbreviate(String string, int maximumLength) {
        Preconditions.checkNotNull(string, "string");
        Preconditions.checkArgument(maximumLength > 0, "length to trim to (%s) is not valid (greater than 0)", maximumLength);
        return string.length() > maximumLength ? string.substring(0, maximumLength - 3) + "..." : string;
    }
}
