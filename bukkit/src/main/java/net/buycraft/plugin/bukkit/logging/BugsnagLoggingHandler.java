package net.buycraft.plugin.bukkit.logging;

import com.bugsnag.Client;
import com.bugsnag.MetaData;
import com.google.common.base.Preconditions;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class BugsnagLoggingHandler extends Handler {
    private final Client client;
    private final BuycraftPlugin plugin;

    private static final Pattern PLUGIN_ERROR = Pattern.compile("Could not dispatch command '(.*)' for player '(.*)'\\. " +
            "This is typically a plugin error, not an issue with BuycraftX\\.");

    public BugsnagLoggingHandler(Client client, BuycraftPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.client = Preconditions.checkNotNull(client, "client");
    }

    @Override
    public void publish(final LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }

        if (PLUGIN_ERROR.matcher(record.getMessage()).find()) {
            return;
        }

        final MetaData data = new MetaData();
        if (plugin.getServerInformation() != null) {
            data.put("account_id", plugin.getServerInformation().getAccount().getId());
            data.put("server_id", plugin.getServerInformation().getServer().getId());
            data.put("platform", "bukkit");
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
