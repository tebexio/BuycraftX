package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueuedCommand that = (QueuedCommand) o;

        if (id != that.id) return false;
        if (paymentId != that.paymentId) return false;
        if (packageId != that.packageId) return false;
        if (!Objects.equals(conditions, that.conditions)) return false;
        if (!Objects.equals(command, that.command)) return false;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + paymentId;
        result = 31 * result + packageId;
        result = 31 * result + (conditions != null ? conditions.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QueuedCommand(id=" + this.getId() + ", paymentId=" + this.getPaymentId() + ", packageId=" + this.getPackageId() + ", conditions=" + this.getConditions() + ", command=" + this.getCommand() + ", player=" + this.getPlayer() + ")";
    }
}
