package net.buycraft.plugin.bungeecord.logging;

import com.bugsnag.Client;
import com.bugsnag.MetaData;
import com.google.common.base.Preconditions;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class BugsnagGlobalLoggingHandler extends Handler {
    private final Client client;
    private final BuycraftPlugin plugin;

    private static final Pattern LISTENER = Pattern.compile("Error dispatching event (.*) to listener net\\.buycraft\\.plugin\\.(.*)");
    private static final Pattern TASK = Pattern.compile("Task (.*)net\\.buycraft\\.plugin\\.(.*) encountered an exception");

    public BugsnagGlobalLoggingHandler(Client client, BuycraftPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.client = Preconditions.checkNotNull(client, "client");
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }

        // Otherwise, see if this error is caused by the Buycraft plugin.
        // THIS DETECTION IS NOT PERFECT, but should catch most cases.
        if (record.getMessage() != null && record.getMessage().equals("Error in dispatching command")) {
            for (StackTraceElement element : record.getThrown().getStackTrace()) {
                if (element.getClassName().equals("net.buycraft.plugin.bungeecord.BuycraftCommand")) {
                    send(record);
                    return;
                }
            }

            return;
        }

        if (record.getMessage() != null && LISTENER.matcher(record.getMessage()).find()) {
            send(record);
            return;
        }

        if (record.getMessage() != null && TASK.matcher(record.getMessage()).find()) {
            send(record);
            return;
        }
    }

    private void send(final LogRecord record) {
        final MetaData data = new MetaData();
        if (plugin.getServerInformation() != null) {
            data.put("account_id", plugin.getServerInformation().getAccount().getId());
            data.put("server_id", plugin.getServerInformation().getServer().getId());
            data.put("platform", "bungeecord");
        }

        if (record.getLevel() == Level.SEVERE) {
            client.notify(record.getThrown(), "error", data);
        } else if (record.getLevel() == Level.WARNING) {
            client.notify(record.getThrown(), "warning", data);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
