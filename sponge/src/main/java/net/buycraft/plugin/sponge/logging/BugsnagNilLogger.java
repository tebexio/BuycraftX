package net.buycraft.plugin.sponge.logging;

import com.bugsnag.Logger;

public class BugsnagNilLogger extends Logger {
    @Override
    public void debug(String message) {
        super.debug(message);
    }

    @Override
    public void info(String message) {
        super.info(message);
    }

    @Override
    public void warn(String message) {
        super.warn(message);
    }

    @Override
    public void warn(String message, Throwable e) {
        super.warn(message, e);
    }

    @Override
    public void warn(Throwable e) {
        super.warn(e);
    }
}
