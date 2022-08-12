package net.buycraft.plugin.fabric;

import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.platform.PlatformInformation;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FabricBuycraftPlatform implements IBuycraftPlatform {
    private final BuycraftPlugin plugin;

    public FabricBuycraftPlatform(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BuyCraftAPI getApiClient() {
        return plugin.getApiClient();
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return plugin.getPlaceholderManager();
    }

    @Override
    public void dispatchCommand(String command) {

    }

    @Override
    public void executeAsync(Runnable runnable) {

    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {

    }

    @Override
    public void executeBlocking(Runnable runnable) {

    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {

    }

    @Override
    public boolean isPlayerOnline(QueuedPlayer player) {
        return false;
    }

    @Override
    public int getFreeSlots(QueuedPlayer player) {
        return 0;
    }

    @Override
    public void log(Level level, String message) {

    }

    @Override
    public void log(Level level, String message, Throwable throwable) {

    }

    @Override
    public CommandExecutor getExecutor() {
        return null;
    }

    @Override
    public PlatformInformation getPlatformInformation() {
        return null;
    }

    @Override
    public String getPluginVersion() {
        return null;
    }

    @Override
    public ServerInformation getServerInformation() {
        return null;
    }

}
