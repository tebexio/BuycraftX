package net.buycraft.plugin.shared.config.signs;

import com.google.common.collect.ImmutableList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import net.buycraft.plugin.data.MaxPayment;

public class MaxPurchaseSignLayout {
    public static final MaxPurchaseSignLayout DEFAULT = new MaxPurchaseSignLayout(ImmutableList.of(
            "",
            "%player%",
            "%amount%",
            "%nbr%" // TODO modify "%nbr% payements"
    ));
    private final List<String> lines;

    public MaxPurchaseSignLayout(List<String> lines) {
        this.lines = ImmutableList.copyOf(lines);
    }

    public List<String> format(MaxPayment p) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setCurrency(Currency.getInstance(p.getCurrency().getIso4217()));
        
        List<String> formatted = new ArrayList<>();
        for (String line : lines) {
            formatted.add(
                    line
                     .replace("%player%", p.getPlayer().getName())
                    .replace("%amount%", format.format(p.getAmount()))
                    .replace("%nbr%", ""+p.getNbrpayment()));
        }
        return formatted;
    }
}
