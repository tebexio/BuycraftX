package io.tebex.sdk.obj;

import com.google.gson.JsonObject;
import io.tebex.sdk.util.StringUtil;

import java.time.ZonedDateTime;

public class CommunityGoal {
    private final int id;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final int accountId;
    private final String name;
    private final String description;
    private final String image;
    private final double target;
    private final double current;
    private final boolean repeatable;
    private final ZonedDateTime lastAchieved;
    private final int timesAchieved;
    private final Status status;
    private final boolean sale;

    /**
     * Constructs a CommunityGoal instance.
     */
    public CommunityGoal(int id, ZonedDateTime createdAt, ZonedDateTime updatedAt, int accountId, String name, String description, String image, double target, double current, boolean repeatable, ZonedDateTime lastAchieved, int timesAchieved, Status status, boolean sale) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accountId = accountId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.target = target;
        this.current = current;
        this.repeatable = repeatable;
        this.lastAchieved = lastAchieved;
        this.timesAchieved = timesAchieved;
        this.status = status;
        this.sale = sale;
    }

    public int getId() {
        return id;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public double getTarget() {
        return target;
    }

    public double getCurrent() {
        return current;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public ZonedDateTime getLastAchieved() {
        return lastAchieved;
    }

    public int getTimesAchieved() {
        return timesAchieved;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasSale() {
        return sale;
    }

    @Override
    public String toString() {
        return "CommunityGoal{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", accountId=" + accountId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", target=" + target +
                ", current=" + current +
                ", repeatable=" + repeatable +
                ", lastAchieved=" + lastAchieved +
                ", timesAchieved=" + timesAchieved +
                ", status='" + status.name() + '\'' +
                ", sale=" + sale +
                '}';
    }

    public enum Status {
        ACTIVE,
        COMPLETED,
        DISABLED
    }

    public static CommunityGoal fromJsonObject(JsonObject jsonObject) {
        return new CommunityGoal(
                jsonObject.get("id").getAsInt(),
                StringUtil.toLegacyDate(jsonObject.get("created_at").getAsString()),
                StringUtil.toLegacyDate(jsonObject.get("updated_at").getAsString()),
                jsonObject.get("account").getAsInt(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("description").getAsString(),
                !jsonObject.get("image").getAsString().isEmpty() ? jsonObject.get("image").getAsString() : null,
                jsonObject.get("target").getAsDouble(),
                jsonObject.get("current").getAsDouble(),
                jsonObject.get("repeatable").getAsInt() != 0,
                !jsonObject.get("last_achieved").isJsonNull() ? StringUtil.toLegacyDate(jsonObject.get("last_achieved").getAsString()) : null,
                jsonObject.get("times_achieved").getAsInt(),
                CommunityGoal.Status.valueOf(jsonObject.get("status").getAsString().toUpperCase()),
                jsonObject.get("sale").getAsBoolean()
        );
    }
}