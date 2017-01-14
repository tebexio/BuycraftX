package net.buycraft.plugin.platform.standalone;

import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.PostCompletedCommandsTask;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;
import net.buycraft.plugin.platform.PlatformType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is an implementation for the {@link net.buycraft.plugin.IBuycraftPlatform} for standalone applications.
 * <p/>
 * You are required to handle logging, command checks, and command dispatching yourself.
 * <p/>
 * Most applications will find {@code StandaloneBuycraftRunner} and {@code StandaloneBuycraftRunnerBuild} easier to use.
 */
public abstract class StandaloneBuycraftPlatform implements IBuycraftPlatform {
    private final ApiClient client;
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    private final QueuedCommandExecutor commandExecutor;
    private final ScheduledExecutorService scheduler;

    protected StandaloneBuycraftPlatform(ApiClient client) {
        this(client, Executors.newScheduledThreadPool(8));
    }

    protected StandaloneBuycraftPlatform(ApiClient client, ScheduledExecutorService executorService) {
        this.client = client;
        this.scheduler = executorService;
        this.placeholderManager.addPlaceholder(new NamePlaceholder());
        this.placeholderManager.addPlaceholder(new UuidPlaceholder());
        PostCompletedCommandsTask completedCommandsTask = new PostCompletedCommandsTask(this);
        this.commandExecutor = new QueuedCommandExecutor(this, completedCommandsTask);
        scheduler.scheduleAtFixedRate(commandExecutor, 50, 50, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(completedCommandsTask, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public ApiClient getApiClient() {
        return client;
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public void executeAsync(Runnable runnable) {
        scheduler.execute(runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        scheduler.schedule(runnable, time, unit);
    }

    @Override
    public void executeBlocking(Runnable runnable) {
        scheduler.execute(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        scheduler.schedule(runnable, time, unit);
    }

    @Override
    public CommandExecutor getExecutor() {
        return commandExecutor;
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return new PlatformInformation(PlatformType.NONE, "");
    }

    @Override
    public String getPluginVersion() {
        return "";
    }

    @Override
    public ServerInformation getServerInformation() {
        return null;
    }
}
