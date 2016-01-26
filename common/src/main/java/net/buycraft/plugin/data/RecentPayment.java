package net.buycraft.plugin.data;

import lombok.Value;
import net.buycraft.plugin.data.responses.ServerInformation;

import java.math.BigDecimal;
import java.util.Calendar;

@Value
public class RecentPayment {
    private final int id;
    private final BigDecimal amount;
    private final Calendar date;
    private final ServerInformation.AccountCurrency currency;
    private final QueuedPlayer player;
}
