package net.buycraft.plugin.sponge.logging;

import com.bugsnag.Bugsnag;
import com.bugsnag.Severity;
import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.util.logging.Level;

@AllArgsConstructor
public class LoggerUtils {
    private final BuycraftPlugin plugin;
    private final Bugsnag bugsnagClient;

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        if (level == Level.INFO) {
            plugin.getLogger().info(message, e);
        } else if (level == Level.WARNING) {
            if (e != null) {
                bugsnagClient.notify(bugsnagClient.buildReport(e)
                        .setSeverity(Severity.WARNING));
            }
            plugin.getLogger().warn(message, e);
        } else if (level == Level.SEVERE) {
            if (e != null) {
                bugsnagClient.notify(bugsnagClient.buildReport(e)
                        .setSeverity(Severity.ERROR));
            }
            plugin.getLogger().error(message, e);
        }
    }
}
