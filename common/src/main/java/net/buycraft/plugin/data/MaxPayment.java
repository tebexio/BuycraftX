package net.buycraft.plugin.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.buycraft.plugin.data.responses.ServerInformation;


@Data @AllArgsConstructor
public class MaxPayment {
    private float amount;
    private int nbrpayment;
    private final ServerInformation.AccountCurrency currency;
    private final QueuedPlayer player;
}
