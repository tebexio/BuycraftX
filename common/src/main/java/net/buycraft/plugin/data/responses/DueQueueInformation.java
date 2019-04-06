package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.List;
import java.util.Objects;

public final class DueQueueInformation {
    private final QueueInformationMeta meta;
    private final List<QueuedPlayer> players;

    public DueQueueInformation(final QueueInformationMeta meta, final List<QueuedPlayer> players) {
        this.meta = meta;
        this.players = players;
    }

    public QueueInformationMeta getMeta() {
        return this.meta;
    }

    public List<QueuedPlayer> getPlayers() {
        return this.players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DueQueueInformation that = (DueQueueInformation) o;

        if (!Objects.equals(meta, that.meta)) return false;
        return Objects.equals(players, that.players);
    }

    @Override
    public int hashCode() {
        int result = meta != null ? meta.hashCode() : 0;
        result = 31 * result + (players != null ? players.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DueQueueInformation(meta=" + this.getMeta() + ", players=" + this.getPlayers() + ")";
    }

    public static final class QueueInformationMeta {
        @SerializedName("execute_offline")
        private final boolean executeOffline;
        @SerializedName("next_check")
        private final int nextCheck;
        private final boolean more;

        public QueueInformationMeta(final boolean executeOffline, final int nextCheck, final boolean more) {
            this.executeOffline = executeOffline;
            this.nextCheck = nextCheck;
            this.more = more;
        }

        public boolean isExecuteOffline() {
            return this.executeOffline;
        }

        public int getNextCheck() {
            return this.nextCheck;
        }

        public boolean isMore() {
            return this.more;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof DueQueueInformation.QueueInformationMeta)) return false;
            final DueQueueInformation.QueueInformationMeta other = (DueQueueInformation.QueueInformationMeta) o;
            if (this.isExecuteOffline() != other.isExecuteOffline()) return false;
            if (this.getNextCheck() != other.getNextCheck()) return false;
            if (this.isMore() != other.isMore()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isExecuteOffline() ? 79 : 97);
            result = result * PRIME + this.getNextCheck();
            result = result * PRIME + (this.isMore() ? 79 : 97);
            return result;
        }

        @Override
        public String toString() {
            return "DueQueueInformation.QueueInformationMeta(executeOffline=" + this.isExecuteOffline() + ", nextCheck=" + this.getNextCheck() + ", more=" + this.isMore() + ")";
        }
    }
}
