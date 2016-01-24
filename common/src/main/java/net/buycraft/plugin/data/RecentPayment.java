package net.buycraft.plugin.data;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;

@Value
public class RecentPayment {
    private final int id;
    private final BigDecimal amount;
    private final Calendar date;
    private final Currency currency;
    private final QueuedPlayer player;
}
