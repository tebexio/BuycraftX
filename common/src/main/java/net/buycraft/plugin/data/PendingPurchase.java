package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PendingPurchase)) return false;
        final PendingPurchase other = (PendingPurchase) o;
        final java.lang.Object this$player = this.getPlayer();
        final java.lang.Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        final java.lang.Object this$aPackage = this.getAPackage();
        final java.lang.Object other$aPackage = other.getAPackage();
        if (this$aPackage == null ? other$aPackage != null : !this$aPackage.equals(other$aPackage)) return false;
        final java.lang.Object this$commands = this.getCommands();
        final java.lang.Object other$commands = other.getCommands();
        if (this$commands == null ? other$commands != null : !this$commands.equals(other$commands)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        final java.lang.Object $aPackage = this.getAPackage();
        result = result * PRIME + ($aPackage == null ? 43 : $aPackage.hashCode());
        final java.lang.Object $commands = this.getCommands();
        result = result * PRIME + ($commands == null ? 43 : $commands.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "PendingPurchase(player=" + this.getPlayer() + ", aPackage=" + this.getAPackage() + ", commands=" + this.getCommands() + ")";
    }
}
