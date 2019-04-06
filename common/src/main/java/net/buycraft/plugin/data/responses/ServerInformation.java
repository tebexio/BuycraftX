package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInformation that = (ServerInformation) o;

        if (!Objects.equals(account, that.account)) return false;
        return Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (server != null ? server.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Account account = (Account) o;

            if (id != account.id) return false;
            if (onlineMode != account.onlineMode) return false;
            if (!Objects.equals(domain, account.domain)) return false;
            if (!Objects.equals(name, account.name)) return false;
            return Objects.equals(currency, account.currency);
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (domain != null ? domain.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (currency != null ? currency.hashCode() : 0);
            result = 31 * result + (onlineMode ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
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
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ServerInformation.Server)) return false;
            final ServerInformation.Server other = (ServerInformation.Server) o;
            if (this.getId() != other.getId()) return false;
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getId();
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            return result;
        }

        @Override
        public String toString() {
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
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ServerInformation.AccountCurrency)) return false;
            final ServerInformation.AccountCurrency other = (ServerInformation.AccountCurrency) o;
            final Object this$iso4217 = this.getIso4217();
            final Object other$iso4217 = other.getIso4217();
            if (this$iso4217 == null ? other$iso4217 != null : !this$iso4217.equals(other$iso4217)) return false;
            final Object this$symbol = this.getSymbol();
            final Object other$symbol = other.getSymbol();
            if (this$symbol == null ? other$symbol != null : !this$symbol.equals(other$symbol)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $iso4217 = this.getIso4217();
            result = result * PRIME + ($iso4217 == null ? 43 : $iso4217.hashCode());
            final Object $symbol = this.getSymbol();
            result = result * PRIME + ($symbol == null ? 43 : $symbol.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "ServerInformation.AccountCurrency(iso4217=" + this.getIso4217() + ", symbol=" + this.getSymbol() + ")";
        }
    }
}
