package net.buycraft.plugin.execution;

import com.google.common.collect.ListMultimap;
import lombok.Value;
import net.buycraft.plugin.data.QueuedCommand;

import java.util.List;

@Value
public class CommandExecutorResult {
    private final List<QueuedCommand> done;
    private final List<QueuedCommand> queuedForOnline;
    private final ListMultimap<Integer, QueuedCommand> queuedForDelay;
}
