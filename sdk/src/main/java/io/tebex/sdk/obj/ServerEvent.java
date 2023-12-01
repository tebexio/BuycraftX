package io.tebex.sdk.obj;

import com.google.gson.annotations.SerializedName;

public class ServerEvent {
    @SerializedName("username_id")
    private final String uuid;

    private final String username;
    private final String ip;

    @SerializedName("event_type")
    private final String eventType;

    @SerializedName("event_date")
    private final String eventDate;

    public ServerEvent(String uuid, String username, String ip, ServerEventType eventType, String eventDate) {
        this.uuid = uuid;
        this.username = username;
        this.ip = ip;
        this.eventType = eventType.getName();
        this.eventDate = eventDate;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getIp() {
        return ip;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventDate() {
        return eventDate;
    }

    @Override
    public String toString() {
        return "ServerEvent{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", ip='" + ip + '\'' +
                ", eventType=" + eventType +
                ", eventDate='" + eventDate + '\'' +
                '}';
    }
}
