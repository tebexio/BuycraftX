package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;

@Value
public class PendingPurchase {
    private final QueuedPlayer player;
    @SerializedName("package")
    private final Package aPackage;
    private final List<String> commands;
}
