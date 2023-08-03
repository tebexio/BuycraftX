package io.tebex.sdk.request.response;

public class ServerInformation {
    private final Store store;
    private final Server server;

    /**
     * Constructs a ServerInformation instance.
     *
     * @param store The store.
     * @param server The server.
     */
    public ServerInformation(Store store, Server server) {
        this.store = store;
        this.server = server;
    }

    /**
     * Returns the store associated.
     *
     * @return The store.
     */
    public Store getStore() {
        return store;
    }

    /**
     * Returns the server associated.
     *
     * @return The server.
     */
    public Server getServer() {
        return server;
    }

    public static class Store {
        private final int id;
        private final String domain;
        private final String name;
        private final Currency currency;
        private final boolean onlineMode;
        private final String gameType;
        private final boolean logEvents;

        /**
         * Constructs a Store instance.
         *
         * @param id The store ID.
         * @param domain The store domain.
         * @param name The store name.
         * @param currency The store currency.
         * @param onlineMode The store online mode.
         * @param gameType The store game type.
         * @param logEvents The store log events.
         */
        public Store(int id, String domain, String name, Currency currency, boolean onlineMode, String gameType, boolean logEvents) {
            this.id = id;
            this.domain = domain;
            this.name = name;
            this.currency = currency;
            this.onlineMode = onlineMode;
            this.gameType = gameType;
            this.logEvents = logEvents;
        }

        public int getId() {
            return id;
        }

        public String getDomain() {
            return domain;
        }

        public String getName() {
            return name;
        }

        public Currency getCurrency() {
            return currency;
        }

        public boolean isOnlineMode() {
            return onlineMode;
        }

        public String getGameType() {
            return gameType;
        }

        public boolean isLogEvents() {
            return logEvents;
        }

        public static class Currency {
            private final String iso4217;
            private final String symbol;

            public Currency(String iso4217, String symbol) {
                this.iso4217 = iso4217;
                this.symbol = symbol;
            }

            public String getIso4217() {
                return iso4217;
            }

            public String getSymbol() {
                return symbol;
            }
        }
    }

    public static class Server {
        private final int id;
        private final String name;

        public Server(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}