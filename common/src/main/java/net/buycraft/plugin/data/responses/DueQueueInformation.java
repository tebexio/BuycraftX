package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;
import net.buycraft.plugin.data.QueuedPlayer;

import java.util.List;

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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof DueQueueInformation)) return false;
        final DueQueueInformation other = (DueQueueInformation) o;
        final java.lang.Object this$meta = this.getMeta();
        final java.lang.Object other$meta = other.getMeta();
        if (this$meta == null ? other$meta != null : !this$meta.equals(other$meta)) return false;
        final java.lang.Object this$players = this.getPlayers();
        final java.lang.Object other$players = other.getPlayers();
        if (this$players == null ? other$players != null : !this$players.equals(other$players)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $meta = this.getMeta();
        result = result * PRIME + ($meta == null ? 43 : $meta.hashCode());
        final java.lang.Object $players = this.getPlayers();
        result = result * PRIME + ($players == null ? 43 : $players.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
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
        public boolean equals(final java.lang.Object o) {
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
        public java.lang.String toString() {
            return "DueQueueInformation.QueueInformationMeta(executeOffline=" + this.isExecuteOffline() + ", nextCheck=" + this.getNextCheck() + ", more=" + this.isMore() + ")";
        }
    }
}
