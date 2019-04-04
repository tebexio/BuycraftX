package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.QueuedCommand;

import java.util.List;

public final class QueueInformation {
    private final OfflineQueueInformationMeta meta;
    private final List<QueuedCommand> commands;

    public QueueInformation(final OfflineQueueInformationMeta meta, final List<QueuedCommand> commands) {
        this.meta = meta;
        this.commands = commands;
    }

    public OfflineQueueInformationMeta getMeta() {
        return this.meta;
    }

    public List<QueuedCommand> getCommands() {
        return this.commands;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof QueueInformation)) return false;
        final QueueInformation other = (QueueInformation) o;
        final java.lang.Object this$meta = this.getMeta();
        final java.lang.Object other$meta = other.getMeta();
        if (this$meta == null ? other$meta != null : !this$meta.equals(other$meta)) return false;
        final java.lang.Object this$commands = this.getCommands();
        final java.lang.Object other$commands = other.getCommands();
        if (this$commands == null ? other$commands != null : !this$commands.equals(other$commands)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $meta = this.getMeta();
        result = result * PRIME + ($meta == null ? 43 : $meta.hashCode());
        final java.lang.Object $commands = this.getCommands();
        result = result * PRIME + ($commands == null ? 43 : $commands.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "QueueInformation(meta=" + this.getMeta() + ", commands=" + this.getCommands() + ")";
    }

    public static final class OfflineQueueInformationMeta {
        private final boolean limited;

        public OfflineQueueInformationMeta(final boolean limited) {
            this.limited = limited;
        }

        public boolean isLimited() {
            return this.limited;
        }

        @Override
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof QueueInformation.OfflineQueueInformationMeta)) return false;
            final QueueInformation.OfflineQueueInformationMeta other = (QueueInformation.OfflineQueueInformationMeta) o;
            if (this.isLimited() != other.isLimited()) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isLimited() ? 79 : 97);
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "QueueInformation.OfflineQueueInformationMeta(limited=" + this.isLimited() + ")";
        }
    }
}
