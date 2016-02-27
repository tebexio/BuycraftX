package net.buycraft.plugin.platform.standalone.runner;

import lombok.Getter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.platform.standalone.StandaloneBuycraftPlatform;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandaloneBuycraftRunner {
    private final CommandDispatcher dispatcher;
    private final PlayerDeterminer determiner;
    private final Logger logger;
    private final ScheduledExecutorService executorService;
    private final IBuycraftPlatform platform;
    @Getter
    private ServerInformation serverInformation;

    StandaloneBuycraftRunner(CommandDispatcher dispatcher, PlayerDeterminer determiner, String apiKey, Logger logger, ScheduledExecutorService executorService) {
        this.dispatcher = dispatcher;
        this.determiner = determiner;
        this.logger = logger;
        this.executorService = executorService;
        this.platform = new StandaloneBuycraftPlatform(new ProductionApiClient(apiKey), executorService) {
            @Override
            public void dispatchCommand(String command) {
                StandaloneBuycraftRunner.this.dispatcher.dispatchCommand(command);
            }

            @Override
            public boolean isPlayerOnline(QueuedPlayer player) {
                return StandaloneBuycraftRunner.this.determiner.isPlayerOnline(player);
            }

            @Override
            public int getFreeSlots(QueuedPlayer player) {
                return StandaloneBuycraftRunner.this.determiner.getFreeSlots(player);
            }

            @Override
            public void log(Level level, String message) {
                StandaloneBuycraftRunner.this.logger.log(level, message);
            }

            @Override
            public void log(Level level, String message, Throwable throwable) {
                StandaloneBuycraftRunner.this.logger.log(level, message, throwable);
            }
        };
    }

    public void initializeTasks() {
        try {
            serverInformation = platform.getApiClient().getServerInformation();
        } catch (IOException | ApiException e) {
            throw new RuntimeException("Can't fetch account information", e);
        }
        executorService.schedule(new DuePlayerFetcher(platform), 1, TimeUnit.SECONDS);
    }
}
