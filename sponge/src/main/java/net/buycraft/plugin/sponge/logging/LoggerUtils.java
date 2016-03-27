package net.buycraft.plugin.sponge.logging;

import com.bugsnag.Client;
import com.bugsnag.MetaData;
import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.Sponge;

import java.util.logging.Level;

@AllArgsConstructor
public class LoggerUtils {
    private final BuycraftPlugin plugin;
    private final Client bugsnagClient;

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        if (e != null) {
            MetaData data = new MetaData();
            if (plugin.getServerInformation() != null) {
                data.put("account_id", plugin.getServerInformation().getAccount().getId());
                data.put("server_id", plugin.getServerInformation().getServer().getId());
                data.put("platform", "sponge");
            }

            bugsnagClient.notify(e, data);
        }

        if (level == Level.INFO) {
            plugin.getLogger().info(message, e);
        } else if (level == Level.WARNING) {
            plugin.getLogger().warn(message, e);
        } else if (level == Level.SEVERE) {
            plugin.getLogger().error(message, e);
        }
    }
}
