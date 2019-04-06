package net.buycraft.plugin.data;

import net.buycraft.plugin.data.responses.ServerInformation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecentPayment that = (RecentPayment) o;

        if (id != that.id) return false;
        if (!Objects.equals(amount, that.amount)) return false;
        if (!Objects.equals(date, that.date)) return false;
        if (!Objects.equals(currency, that.currency)) return false;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RecentPayment(id=" + this.getId() + ", amount=" + this.getAmount() + ", date=" + this.getDate() + ", currency=" + this.getCurrency() + ", player=" + this.getPlayer() + ")";
    }
}
