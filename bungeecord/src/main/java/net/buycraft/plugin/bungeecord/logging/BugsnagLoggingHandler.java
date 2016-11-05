package net.buycraft.plugin.bungeecord.logging;

import com.bugsnag.Bugsnag;
import com.bugsnag.Severity;
import com.google.common.base.Preconditions;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class BugsnagLoggingHandler extends Handler {
    private static final Pattern PLUGIN_ERROR = Pattern.compile("Could not dispatch command '(.*)' for player '(.*)'\\. " +
            "This is typically a plugin error, not an issue with BuycraftX\\.");
    private final Bugsnag client;
    private final BuycraftPlugin plugin;

    public BugsnagLoggingHandler(Bugsnag client, BuycraftPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.client = Preconditions.checkNotNull(client, "client");
    }

    @Override
    public void publish(final LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }

        // BungeeCord logs this message if it can't execute a command.
        if (record.getMessage().equals("Error in dispatching command")) {
            return;
        }

        if (PLUGIN_ERROR.matcher(record.getMessage()).find()) {
            return;
        }

        if (record.getLevel() == Level.SEVERE) {
            client.notify(client.buildReport(record.getThrown())
                    .setSeverity(Severity.ERROR));
        } else if (record.getLevel() == Level.WARNING) {
            client.notify(client.buildReport(record.getThrown())
                    .setSeverity(Severity.WARNING));
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
