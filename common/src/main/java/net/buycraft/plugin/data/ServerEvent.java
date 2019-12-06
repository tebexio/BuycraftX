package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ServerEvent {

    public static final String JOIN_EVENT = "server.join";
    public static final String LEAVE_EVENT = "server.leave";

    @SerializedName("username_id")
    private String uuid;

    private String username;

    private String ip;

    @SerializedName("event_type")
    private String eventType;

    @SerializedName("event_date")
    private Date eventDate;

    public ServerEvent(String uuid, String username, String ip, String eventType, Date eventDate) {
        this.uuid = uuid;
        this.username = username;
        this.ip = ip;
        this.eventType = eventType;
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

    public Date getEventDate() {
        return eventDate;
    }
}
