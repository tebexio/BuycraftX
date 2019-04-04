package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;

public final class ServerInformation {
    private final Account account;
    private final Server server;

    public ServerInformation(final Account account, final Server server) {
        this.account = account;
        this.server = server;
    }

    public Account getAccount() {
        return this.account;
    }

    public Server getServer() {
        return this.server;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerInformation)) return false;
        final ServerInformation other = (ServerInformation) o;
        final java.lang.Object this$account = this.getAccount();
        final java.lang.Object other$account = other.getAccount();
        if (this$account == null ? other$account != null : !this$account.equals(other$account)) return false;
        final java.lang.Object this$server = this.getServer();
        final java.lang.Object other$server = other.getServer();
        if (this$server == null ? other$server != null : !this$server.equals(other$server)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $account = this.getAccount();
        result = result * PRIME + ($account == null ? 43 : $account.hashCode());
        final java.lang.Object $server = this.getServer();
        result = result * PRIME + ($server == null ? 43 : $server.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "ServerInformation(account=" + this.getAccount() + ", server=" + this.getServer() + ")";
    }

    public static final class Account {
        private final int id;
        private final String domain;
        private final String name;
        private final AccountCurrency currency;
        @SerializedName("online_mode")
        private final boolean onlineMode;

        public Account(final int id, final String domain, final String name, final AccountCurrency currency, final boolean onlineMode) {
            this.id = id;
            this.domain = domain;
            this.name = name;
            this.currency = currency;
            this.onlineMode = onlineMode;
        }

        public int getId() {
            return this.id;
        }

        public String getDomain() {
            return this.domain;
        }

        public String getName() {
            return this.name;
        }

        public AccountCurrency getCurrency() {
            return this.currency;
        }

        public boolean isOnlineMode() {
            return this.onlineMode;
        }

        @Override
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof ServerInformation.Account)) return false;
            final ServerInformation.Account other = (ServerInformation.Account) o;
            if (this.getId() != other.getId()) return false;
            final java.lang.Object this$domain = this.getDomain();
            final java.lang.Object other$domain = other.getDomain();
            if (this$domain == null ? other$domain != null : !this$domain.equals(other$domain)) return false;
            final java.lang.Object this$name = this.getName();
            final java.lang.Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            final java.lang.Object this$currency = this.getCurrency();
            final java.lang.Object other$currency = other.getCurrency();
            if (this$currency == null ? other$currency != null : !this$currency.equals(other$currency)) return false;
            if (this.isOnlineMode() != other.isOnlineMode()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getId();
            final java.lang.Object $domain = this.getDomain();
            result = result * PRIME + ($domain == null ? 43 : $domain.hashCode());
            final java.lang.Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final java.lang.Object $currency = this.getCurrency();
            result = result * PRIME + ($currency == null ? 43 : $currency.hashCode());
            result = result * PRIME + (this.isOnlineMode() ? 79 : 97);
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "ServerInformation.Account(id=" + this.getId() + ", domain=" + this.getDomain() + ", name=" + this.getName() + ", currency=" + this.getCurrency() + ", onlineMode=" + this.isOnlineMode() + ")";
        }
    }

    public static final class Server {
        private final int id;
        private final String name;

        public Server(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof ServerInformation.Server)) return false;
            final ServerInformation.Server other = (ServerInformation.Server) o;
            if (this.getId() != other.getId()) return false;
            final java.lang.Object this$name = this.getName();
            final java.lang.Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getId();
            final java.lang.Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "ServerInformation.Server(id=" + this.getId() + ", name=" + this.getName() + ")";
        }
    }

    public static final class AccountCurrency {
        @SerializedName("iso_4217")
        private final String iso4217;
        private final String symbol;

        public AccountCurrency(final String iso4217, final String symbol) {
            this.iso4217 = iso4217;
            this.symbol = symbol;
        }

        public String getIso4217() {
            return this.iso4217;
        }

        public String getSymbol() {
            return this.symbol;
        }

        @Override
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof ServerInformation.AccountCurrency)) return false;
            final ServerInformation.AccountCurrency other = (ServerInformation.AccountCurrency) o;
            final java.lang.Object this$iso4217 = this.getIso4217();
            final java.lang.Object other$iso4217 = other.getIso4217();
            if (this$iso4217 == null ? other$iso4217 != null : !this$iso4217.equals(other$iso4217)) return false;
            final java.lang.Object this$symbol = this.getSymbol();
            final java.lang.Object other$symbol = other.getSymbol();
            if (this$symbol == null ? other$symbol != null : !this$symbol.equals(other$symbol)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $iso4217 = this.getIso4217();
            result = result * PRIME + ($iso4217 == null ? 43 : $iso4217.hashCode());
            final java.lang.Object $symbol = this.getSymbol();
            result = result * PRIME + ($symbol == null ? 43 : $symbol.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "ServerInformation.AccountCurrency(iso4217=" + this.getIso4217() + ", symbol=" + this.getSymbol() + ")";
        }
    }
}
