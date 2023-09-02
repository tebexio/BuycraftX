package io.tebex.sdk.obj;

public enum ServerEventType {
    JOIN("server.join"),
    LEAVE("server.leave");

    private final String name;

    ServerEventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
