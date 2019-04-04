package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public final class QueuedCommand {
    private final int id;
    @SerializedName("payment")
    private final int paymentId;
    @SerializedName("package")
    private final int packageId;
    private final Map<String, Integer> conditions;
    private final String command;
    private final QueuedPlayer player;

    public QueuedCommand(final int id, final int paymentId, final int packageId, final Map<String, Integer> conditions, final String command, final QueuedPlayer player) {
        this.id = id;
        this.paymentId = paymentId;
        this.packageId = packageId;
        this.conditions = conditions;
        this.command = command;
        this.player = player;
    }

    public int getId() {
        return this.id;
    }

    public int getPaymentId() {
        return this.paymentId;
    }

    public int getPackageId() {
        return this.packageId;
    }

    public Map<String, Integer> getConditions() {
        return this.conditions;
    }

    public String getCommand() {
        return this.command;
    }

    public QueuedPlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof QueuedCommand)) return false;
        final QueuedCommand other = (QueuedCommand) o;
        if (this.getId() != other.getId()) return false;
        if (this.getPaymentId() != other.getPaymentId()) return false;
        if (this.getPackageId() != other.getPackageId()) return false;
        final java.lang.Object this$conditions = this.getConditions();
        final java.lang.Object other$conditions = other.getConditions();
        if (this$conditions == null ? other$conditions != null : !this$conditions.equals(other$conditions))
            return false;
        final java.lang.Object this$command = this.getCommand();
        final java.lang.Object other$command = other.getCommand();
        if (this$command == null ? other$command != null : !this$command.equals(other$command)) return false;
        final java.lang.Object this$player = this.getPlayer();
        final java.lang.Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        result = result * PRIME + this.getPaymentId();
        result = result * PRIME + this.getPackageId();
        final java.lang.Object $conditions = this.getConditions();
        result = result * PRIME + ($conditions == null ? 43 : $conditions.hashCode());
        final java.lang.Object $command = this.getCommand();
        result = result * PRIME + ($command == null ? 43 : $command.hashCode());
        final java.lang.Object $player = this.getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "QueuedCommand(id=" + this.getId() + ", paymentId=" + this.getPaymentId() + ", packageId=" + this.getPackageId() + ", conditions=" + this.getConditions() + ", command=" + this.getCommand() + ", player=" + this.getPlayer() + ")";
    }
}
