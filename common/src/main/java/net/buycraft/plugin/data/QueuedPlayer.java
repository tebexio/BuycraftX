package net.buycraft.plugin.data;

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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof QueuedPlayer)) return false;
        final QueuedPlayer other = (QueuedPlayer) o;
        if (this.getId() != other.getId()) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$uuid = this.getUuid();
        final java.lang.Object other$uuid = other.getUuid();
        if (this$uuid == null ? other$uuid != null : !this$uuid.equals(other$uuid)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $uuid = this.getUuid();
        result = result * PRIME + ($uuid == null ? 43 : $uuid.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "QueuedPlayer(id=" + this.getId() + ", name=" + this.getName() + ", uuid=" + this.getUuid() + ")";
    }
}
