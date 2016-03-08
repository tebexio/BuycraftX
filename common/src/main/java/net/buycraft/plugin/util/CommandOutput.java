package net.buycraft.plugin.util;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandOutput {
    private final List<String> outputResults = new ArrayList<>();
    private final AtomicBoolean commandExecutionComplete = new AtomicBoolean(false);

    public void addLine(String line) {
        if (!commandExecutionComplete.get())
            outputResults.add(line);
    }

    public String asNewlined() {
        return Joiner.on('\n').join(outputResults);
    }

    public void finish() {
        commandExecutionComplete.set(true);
    }
}
