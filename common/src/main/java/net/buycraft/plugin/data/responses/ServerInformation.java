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
        @SerializedName("log_events")
        private final boolean logEvents;

        public Account(int id, String domain, String name, AccountCurrency currency, boolean onlineMode, boolean logEvents) {
            this.id = id;
            this.domain = domain;
            this.name = name;
            this.currency = currency;
            this.onlineMode = onlineMode;
            this.logEvents = logEvents;
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

        public boolean isLogEvents() {
            return logEvents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Account account = (Account) o;

            if (id != account.id) return false;
            if (onlineMode != account.onlineMode) return false;
            if (logEvents != account.logEvents) return false;
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
            result = 31 * result + (logEvents ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Account(" + "id=" + id + ", domain='" + domain + '\'' + ", name='" + name + '\'' + ", currency=" + currency + ", onlineMode=" + onlineMode + ", logEvents=" + logEvents + ')';
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Server server = (Server) o;

            if (id != server.id) return false;
            return Objects.equals(name, server.name);
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AccountCurrency that = (AccountCurrency) o;

            if (!Objects.equals(iso4217, that.iso4217)) return false;
            return Objects.equals(symbol, that.symbol);
        }

        @Override
        public int hashCode() {
            int result = iso4217 != null ? iso4217.hashCode() : 0;
            result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ServerInformation.AccountCurrency(iso4217=" + this.getIso4217() + ", symbol=" + this.getSymbol() + ")";
        }
    }
}
