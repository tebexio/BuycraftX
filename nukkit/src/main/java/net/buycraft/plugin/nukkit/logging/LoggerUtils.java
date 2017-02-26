package net.buycraft.plugin.nukkit.logging;

import cn.nukkit.plugin.PluginLogger;
import com.bugsnag.Bugsnag;
import com.bugsnag.Severity;
import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;

import java.util.logging.Level;

@AllArgsConstructor
public class LoggerUtils {
    private final PluginLogger logger;
    private final Bugsnag bugsnagClient;

    public void log(Level level, String message) {
        log(level, message, null);
    }

    public void log(Level level, String message, Throwable e) {
        if (level == Level.INFO) {
            logger.info(message, e);
        } else if (level == Level.WARNING) {
            if (e != null) {
                bugsnagClient.notify(bugsnagClient.buildReport(e)
                        .setSeverity(Severity.WARNING));
            }
            logger.warning(message, e);
        } else if (level == Level.SEVERE) {
            if (e != null) {
                bugsnagClient.notify(bugsnagClient.buildReport(e)
                        .setSeverity(Severity.ERROR));
            }
            logger.error(message, e);
        }
    }
}
