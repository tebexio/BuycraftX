package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.List;

@Value
public class DueQueueInformation {
    private final QueueInformationMeta meta;
    private final List<QueuedPlayer> players;

    @Value
    public static class QueueInformationMeta {
        @SerializedName("execute_offline")
        private final boolean executeOffline;
        @SerializedName("next_check")
        private final int nextCheck;
        private final boolean more;
    }
}
