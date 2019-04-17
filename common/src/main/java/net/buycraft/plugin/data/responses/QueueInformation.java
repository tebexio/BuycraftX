package net.buycraft.plugin.data.responses;

import net.buycraft.plugin.data.QueuedCommand;

import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueueInformation that = (QueueInformation) o;

        if (!Objects.equals(meta, that.meta)) return false;
        return Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        int result = meta != null ? meta.hashCode() : 0;
        result = 31 * result + (commands != null ? commands.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OfflineQueueInformationMeta that = (OfflineQueueInformationMeta) o;

            return limited == that.limited;
        }

        @Override
        public int hashCode() {
            return (limited ? 1 : 0);
        }

        @Override
        public String toString() {
            return "QueueInformation.OfflineQueueInformationMeta(limited=" + this.isLimited() + ")";
        }
    }
}
