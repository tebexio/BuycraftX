package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GiftCard {
    private final int id;
    private final String code;
    private final String note;
    @SerializedName("void")
    private final boolean isVoid;

    @Value
    public static class Balance {
        private final String starting;
        private final String remaining;
        private final String currency;
    }
}
