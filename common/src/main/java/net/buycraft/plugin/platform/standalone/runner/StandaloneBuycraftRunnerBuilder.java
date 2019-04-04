package net.buycraft.plugin.platform.standalone.runner;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

/**
 * This class allows for simple handling and execution of Buycraft commands with a fluent API.
 */
public class StandaloneBuycraftRunnerBuilder {
    @NotNull
    private final CommandDispatcher dispatcher;
    @NotNull
    private final PlayerDeterminer determiner;
    @NotNull
    private final String apiKey;
    @NotNull
    private final Logger logger;
    @NotNull
    private final ScheduledExecutorService executorService;
    @NotNull
    private boolean verbose = true;

    StandaloneBuycraftRunnerBuilder(@NotNull final CommandDispatcher dispatcher, @NotNull final PlayerDeterminer determiner, @NotNull final String apiKey, @NotNull final Logger logger, @NotNull final ScheduledExecutorService executorService, @NotNull final boolean verbose) {
        if (dispatcher == null) {
            throw new java.lang.NullPointerException("dispatcher is marked @NotNull but is null");
        }
        if (determiner == null) {
            throw new java.lang.NullPointerException("determiner is marked @NotNull but is null");
        }
        if (apiKey == null) {
            throw new java.lang.NullPointerException("apiKey is marked @NotNull but is null");
        }
        if (logger == null) {
            throw new java.lang.NullPointerException("logger is marked @NotNull but is null");
        }
        if (executorService == null) {
            throw new java.lang.NullPointerException("executorService is marked @NotNull but is null");
        }
        this.dispatcher = dispatcher;
        this.determiner = determiner;
        this.apiKey = apiKey;
        this.logger = logger;
        this.executorService = executorService;
        this.verbose = verbose;
    }

    public static StandaloneBuycraftRunnerBuilderBuilder builder() {
        return new StandaloneBuycraftRunnerBuilderBuilder();
    }

    public StandaloneBuycraftRunner start() {
        StandaloneBuycraftRunner runner = new StandaloneBuycraftRunner(dispatcher, determiner, apiKey, logger, executorService, verbose);
        runner.initializeTasks();
        return runner;
    }

    public static class StandaloneBuycraftRunnerBuilderBuilder {
        private CommandDispatcher dispatcher;
        private PlayerDeterminer determiner;
        private String apiKey;
        private Logger logger;
        private ScheduledExecutorService executorService;
        private boolean verbose;

        StandaloneBuycraftRunnerBuilderBuilder() {
        }

        public StandaloneBuycraftRunnerBuilderBuilder dispatcher(@NotNull final CommandDispatcher dispatcher) {
            if (dispatcher == null) {
                throw new java.lang.NullPointerException("dispatcher is marked @NotNull but is null");
            }
            this.dispatcher = dispatcher;
            return this;
        }

        public StandaloneBuycraftRunnerBuilderBuilder determiner(@NotNull final PlayerDeterminer determiner) {
            if (determiner == null) {
                throw new java.lang.NullPointerException("determiner is marked @NotNull but is null");
            }
            this.determiner = determiner;
            return this;
        }

        public StandaloneBuycraftRunnerBuilderBuilder apiKey(@NotNull final String apiKey) {
            if (apiKey == null) {
                throw new java.lang.NullPointerException("apiKey is marked @NotNull but is null");
            }
            this.apiKey = apiKey;
            return this;
        }

        public StandaloneBuycraftRunnerBuilderBuilder logger(@NotNull final Logger logger) {
            if (logger == null) {
                throw new java.lang.NullPointerException("logger is marked @NotNull but is null");
            }
            this.logger = logger;
            return this;
        }

        public StandaloneBuycraftRunnerBuilderBuilder executorService(@NotNull final ScheduledExecutorService executorService) {
            if (executorService == null) {
                throw new java.lang.NullPointerException("executorService is marked @NotNull but is null");
            }
            this.executorService = executorService;
            return this;
        }

        public StandaloneBuycraftRunnerBuilderBuilder verbose(@NotNull final boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public StandaloneBuycraftRunnerBuilder build() {
            return new StandaloneBuycraftRunnerBuilder(dispatcher, determiner, apiKey, logger, executorService, verbose);
        }

        @Override
        public java.lang.String toString() {
            return "StandaloneBuycraftRunnerBuilder.StandaloneBuycraftRunnerBuilderBuilder(dispatcher=" + this.dispatcher + ", determiner=" + this.determiner + ", apiKey=" + this.apiKey + ", logger=" + this.logger + ", executorService=" + this.executorService + ", verbose=" + this.verbose + ")";
        }
    }
}
