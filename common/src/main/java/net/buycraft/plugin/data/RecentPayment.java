package net.buycraft.plugin.data;

import net.buycraft.plugin.data.responses.ServerInformation;

import java.math.BigDecimal;
import java.util.Date;

public final class RecentPayment {
    private final int id;
    private final BigDecimal amount;
    private final Date date;
    private final ServerInformation.AccountCurrency currency;
    private final QueuedPlayer player;

    public RecentPayment(final int id, final BigDecimal amount, final Date date, final ServerInformation.AccountCurrency currency, final QueuedPlayer player) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.currency = currency;
        this.player = player;
    }

    public int getId() {
        return this.id;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Date getDate() {
        return this.date;
    }

    public ServerInformation.AccountCurrency getCurrency() {
        return this.currency;
    }

    public QueuedPlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof RecentPayment)) return false;
        final RecentPayment other = (RecentPayment) o;
        if (this.getId() != other.getId()) return false;
        final java.lang.Object this$amount = this.getAmount();
        final java.lang.Object other$amount = other.getAmount();
        if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
        final java.lang.Object this$date = this.getDate();
        final java.lang.Object other$date = other.getDate();
        if (this$date == null ? other$date != null : !this$date.equals(other$date)) return false;
        final java.lang.Object this$currency = this.getCurrency();
        final java.lang.Object other$currency = other.getCurrency();
        if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) return false;
        final java.lang.Object this$player = this.getPlayer();
        final java.lang.Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final java.lang.Object $amount = this.getAmount();
        result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
        final java.lang.Object $date = this.getDate();
        result = result * PRIME + ($date == null ? 43 : $date.hashCode());
        final java.lang.Object $currency = this.getCurrency();
        result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
        final java.lang.Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "RecentPayment(id=" + this.getId() + ", amount=" + this.getAmount() + ", date=" + this.getDate() + ", currency=" + this.getCurrency() + ", player=" + this.getPlayer() + ")";
    }
}
