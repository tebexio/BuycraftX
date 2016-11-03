package net.buycraft.plugin.shared.config.signs;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.data.RecentPayment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class RecentPurchaseSignLayout {
    public static final RecentPurchaseSignLayout DEFAULT = new RecentPurchaseSignLayout(ImmutableList.of(
            "",
            "%player%",
            "%amount%"
    ));
    private final List<String> lines;

    public RecentPurchaseSignLayout(List<String> lines) {
        this.lines = ImmutableList.copyOf(lines);
    }

    public List<String> format(RecentPayment p) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setCurrency(Currency.getInstance(p.getCurrency().getIso4217()));

        List<String> formatted = new ArrayList<>();
        for (String line : lines) {
            formatted.add(line.replace("%player%", p.getPlayer().getName())
                    .replace("%amount%", format.format(p.getAmount())));
        }
        return formatted;
    }
}
