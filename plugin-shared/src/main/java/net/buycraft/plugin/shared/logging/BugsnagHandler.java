package net.buycraft.plugin.shared.logging;

import com.bugsnag.Bugsnag;
import com.bugsnag.Severity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class BugsnagHandler extends Handler {
    private static final List<String> EXCLUDE_CLASSES = ImmutableList.of(
            "org.bukkit.command.CommandException" // Bukkit
    );
    private static final Pattern BUYCRAFT_COMMAND_ERROR = Pattern.compile("Could not dispatch command '(.*)' for player '(.*)'\\. " +
            "This is typically a plugin error, not an issue with BuycraftX\\.");
    private final Bugsnag client;

    public BugsnagHandler(Bugsnag client) {
        this.client = Preconditions.checkNotNull(client, "client");
    }

    @Override
    public void publish(final LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }

        if (record.getMessage() != null) {
            // BungeeCord logs this message if it can't execute a command.
            if (record.getMessage().equals("Error in dispatching command")) {
                return;
            }

            // Buycraft logs this message if an exception is raised while trying to run a command.
            if (BUYCRAFT_COMMAND_ERROR.matcher(record.getMessage()).find()) {
                return;
            }
        }

        // Check if this exception is not allowed.
        if (EXCLUDE_CLASSES.contains(record.getThrown().getClass().getName())) {
            return;
        }

        if (isRelevant(record.getThrown()) || isRelevant(record.getThrown().getCause())) {
            if (record.getLevel() == Level.SEVERE) {
                client.notify(client.buildReport(record.getThrown())
                        .setSeverity(Severity.ERROR));
            } else if (record.getLevel() == Level.WARNING) {
                client.notify(client.buildReport(record.getThrown())
                        .setSeverity(Severity.WARNING));
            }
        }
    }

    private boolean isRelevant(Throwable throwable) {
        if (throwable == null) return false;
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (element.getClassName().startsWith("net.buycraft.plugin")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
