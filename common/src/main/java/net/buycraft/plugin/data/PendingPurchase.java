package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public final class PendingPurchase {
    private final QueuedPlayer player;
    @SerializedName("package")
    private final Package aPackage;
    private final List<String> commands;

    public PendingPurchase(final QueuedPlayer player, final Package aPackage, final List<String> commands) {
        this.player = player;
        this.aPackage = aPackage;
        this.commands = commands;
    }

    public QueuedPlayer getPlayer() {
        return this.player;
    }

    public Package getAPackage() {
        return this.aPackage;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingPurchase that = (PendingPurchase) o;

        if (!Objects.equals(player, that.player)) return false;
        if (!Objects.equals(aPackage, that.aPackage)) return false;
        return Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (aPackage != null ? aPackage.hashCode() : 0);
        result = 31 * result + (commands != null ? commands.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PendingPurchase(player=" + this.getPlayer() + ", aPackage=" + this.getAPackage() + ", commands=" + this.getCommands() + ")";
    }
}
