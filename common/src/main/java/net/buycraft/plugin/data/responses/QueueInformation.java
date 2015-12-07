package net.buycraft.plugin.data.responses;

import lombok.Value;
import net.buycraft.plugin.data.QueuedCommand;

import java.util.List;

@Value
public class QueueInformation {
    private final OfflineQueueInformationMeta meta;
    private final List<QueuedCommand> commands;

    @Value
    public static class OfflineQueueInformationMeta {
        private final boolean limited;
    }
}
