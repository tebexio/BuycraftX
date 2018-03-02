package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Value
@Builder
public class Coupon {
    private final int id;
    private final String code;
    private final Effective effective;
    private final Discount discount;
    private final Expire expire;
    @SerializedName("basket_type")
    private final String basketType;
    @SerializedName("start_date")
    private final Date startDate;
    @SerializedName("user_limit")
    private final int userLimit;
    @SerializedName("discount_application_method")
    private final int discountMethod;
    @SerializedName("expire_never")
    private final int expireNever;
    @SerializedName("redeem_unlimited")
    private final int redeemUnlimited;

    private final BigDecimal minimum;

    @Value
    public static class Effective {
        private final String type;
        private final List<Integer> packages;
        private final List<Integer> categories;
    }

    @Value
    public static class Discount {
        private final String type;
        private final BigDecimal percentage;
        private final BigDecimal value;
    }

    @Value
    public static class Expire {
        private final String type;
        private final int limit;
        private final Date date;
    }
}
