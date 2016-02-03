package net.buycraft.plugin.bukkit.logging;

import com.bugsnag.Client;
import com.bugsnag.MetaData;
import com.google.common.base.Preconditions;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class BugsnagLoggingHandler extends Handler {
    private final Client client;
    private final BuycraftPlugin plugin;

    public BugsnagLoggingHandler(Client client, BuycraftPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin");
        this.client = Preconditions.checkNotNull(client, "client");
    }

    @Override
    public void publish(final LogRecord record) {
        if (record.getThrown() == null) {
            return;
        }

        final MetaData data = new MetaData();
        if (plugin.getServerInformation() != null) {
            data.put("account_id", plugin.getServerInformation().getAccount().getId());
            data.put("server_id", plugin.getServerInformation().getServer().getId());
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (record.getLevel() == Level.SEVERE) {
                    client.notify(record.getThrown(), "error", data);
                } else if (record.getLevel() == Level.WARNING) {
                    client.notify(record.getThrown(), "warning", data);
                }
            }
        });
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
