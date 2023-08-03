package io.tebex.sdk.obj;

import com.google.gson.JsonObject;

public class QueuedPlayer {
    private final int id;
    private final String name;
    private final String uuid;

    /**
     * Constructs a Player instance.
     *
     * @param id The Tebex player ID.
     * @param name The player name.
     * @param uuid The player UUID.
     */
    public QueuedPlayer(int id, String name, String uuid) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public static QueuedPlayer fromJson(JsonObject object) {
        return new QueuedPlayer(
                object.get("id").getAsInt(),
                object.get("name").getAsString(),
                !object.get("uuid").isJsonNull() ? object.get("uuid").getAsString() : null
        );
    }
}
