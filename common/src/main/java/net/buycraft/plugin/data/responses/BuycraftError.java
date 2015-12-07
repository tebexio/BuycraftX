package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class BuycraftError {
    @SerializedName("error_code")
    private final int errorCode;
    @SerializedName("error_message")
    private final String errorMessage;
}
