package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.Map;

@Value
public class QueuedCommand {
    private final int id;
    @SerializedName("payment")
    private final int paymentId;
    @SerializedName("package")
    private final int packageId;
    private final Map<String, Integer> conditions;
    private final String command;
    private final QueuedPlayer player;
}
