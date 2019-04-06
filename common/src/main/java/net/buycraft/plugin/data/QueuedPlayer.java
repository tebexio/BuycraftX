package net.buycraft.plugin.data;

import java.util.Objects;

public final class QueuedPlayer {
    private final int id;
    private final String name;
    private final String uuid;

    public QueuedPlayer(final int id, final String name, final String uuid) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueuedPlayer that = (QueuedPlayer) o;

        if (id != that.id) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QueuedPlayer(id=" + this.getId() + ", name=" + this.getName() + ", uuid=" + this.getUuid() + ")";
    }
}
