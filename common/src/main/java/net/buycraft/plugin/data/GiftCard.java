package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public final class GiftCard {

    private final int id = -1;
    private final String code = null;
    private final Balance balance;
    private final String note;
    @SerializedName("void")
    private final boolean isVoid = false;

    GiftCard(BigDecimal amount, String note) {
        this.balance = new Balance(amount);
        this.note = note;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Balance getBalance() {
        return balance;
    }

    public String getNote() {
        return note;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public static final class Balance {

        private final BigDecimal starting;
        private final BigDecimal remaining;
        private final String currency = null;

        Balance(BigDecimal base) {
            this.starting = base;
            this.remaining = base;
        }

        public BigDecimal getStarting() {
            return starting;
        }

        public BigDecimal getRemaining() {
            return remaining;
        }

        public String getCurrency() {
            return currency;
        }
    }

    public static class Builder {
        private BigDecimal amount;
        private String note;

        Builder() {
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public GiftCard build() {
            return new GiftCard(amount, note);
        }

        @Override
        public String toString() {
            return "GiftCard.Builder(amount=" + amount + ", note=" + note + ")";
        }
    }

}
