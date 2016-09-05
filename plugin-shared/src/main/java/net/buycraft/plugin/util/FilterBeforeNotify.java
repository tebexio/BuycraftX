package net.buycraft.plugin.util;

import com.bugsnag.BeforeNotify;
import com.bugsnag.Error;

public class FilterBeforeNotify implements BeforeNotify {
    @Override
    public boolean run(Error error) {
        for (StackTraceElement element : error.getStackTrace()) {
            // Send only errors that mention a BuycraftX stack trace.
            if (element.getClassName().startsWith("net.buycraft.plugin")) {
                return true;
            }
        }

        return false;
    }
}
