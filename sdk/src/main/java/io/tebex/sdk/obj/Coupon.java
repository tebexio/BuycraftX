package io.tebex.sdk.obj;

import com.google.gson.JsonObject;
import io.tebex.sdk.util.GsonUtil;
import io.tebex.sdk.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.List;

public class Coupon {
    private final int id;
    private final String code;
    private final Effective effective;
    private final Discount discount;
    private final Expiry expiry;
    private final BasketType basketType;
    private final ZonedDateTime startDate;
    private final int userLimit;
    private final int minimum;
    private final String username;
    private final String note;

    public Coupon(int id, String code, Effective effective, Discount discount, Expiry expiry, BasketType basketType, ZonedDateTime startDate, int userLimit, int minimum, String username, String note) {
        this.id = id;
        this.code = code;
        this.effective = effective;
        this.discount = discount;
        this.expiry = expiry;
        this.basketType = basketType;
        this.startDate = startDate;
        this.userLimit = userLimit;
        this.minimum = minimum;
        this.username = username;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Effective getEffective() {
        return effective;
    }

    public Discount getDiscount() {
        return discount;
    }

    public Expiry getExpiry() {
        return expiry;
    }

    public BasketType getBasketType() {
        return basketType;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public int getMinimum() {
        return minimum;
    }

    public String getUsername() {
        return username;
    }

    public String getNote() {
        return note;
    }

    public static class Effective {
        private final EffectiveType type;
        private final List<Integer> packages;
        private final List<Integer> categories;

        public Effective(EffectiveType type, List<Integer> packages, List<Integer> categories) {
            this.type = type;
            this.packages = packages;
            this.categories = categories;
        }

        public EffectiveType getType() {
            return type;
        }

        public List<Integer> getPackages() {
            return packages;
        }

        public List<Integer> getCategories() {
            return categories;
        }

        public enum EffectiveType {
            PACKAGE,
            CATEGORY
        }

        @Override
        public String toString() {
            return "Effective{" +
                    "type='" + type + '\'' +
                    ", packages=" + packages +
                    ", categories=" + categories +
                    '}';
        }
    }

    public static class Discount {
        private final DiscountType type;
        private final double percentage;
        private final int value;

        public Discount(DiscountType type, double percentage, int value) {
            this.type = type;
            this.percentage = percentage;
            this.value = value;
        }

        public DiscountType getType() {
            return type;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Discount{" +
                    "type='" + type + '\'' +
                    ", percentage=" + percentage +
                    ", value=" + value +
                    '}';
        }
    }

    public static class Expiry {
        private final boolean redeemUnlimited;
        private final boolean neverExpires;
        private final int limit;
        private final ZonedDateTime date;

        public Expiry(boolean redeemUnlimited, boolean neverExpires, int limit, ZonedDateTime date) {
            this.redeemUnlimited = redeemUnlimited;
            this.neverExpires = neverExpires;
            this.limit = limit;
            this.date = date;
        }

        public boolean canRedeemUnlimited() {
            return redeemUnlimited;
        }

        public boolean canExpire() {
            return !neverExpires;
        }

        public int getLimit() {
            return limit;
        }

        public ZonedDateTime getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Expiry{" +
                    "redeemUnlimited=" + redeemUnlimited +
                    ", neverExpires=" + neverExpires +
                    ", limit=" + limit +
                    ", date=" + date +
                    '}';
        }
    }

    public static Coupon fromJsonObject(JsonObject jsonObject) {
        JsonObject effectiveJson = jsonObject.get("effective").getAsJsonObject();
        JsonObject discountJson = jsonObject.get("discount").getAsJsonObject();
        JsonObject expireJson = jsonObject.get("expire").getAsJsonObject();

        return new Coupon(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("code").getAsString(),
                new Effective(
                        Effective.EffectiveType.valueOf(effectiveJson.get("type").getAsString().toUpperCase()),
                        GsonUtil.arrayToList(effectiveJson.get("packages").getAsJsonArray()),
                        GsonUtil.arrayToList(effectiveJson.get("categories").getAsJsonArray())
                ),
                new Discount(
                        DiscountType.valueOf(discountJson.get("type").getAsString().toUpperCase()),
                        discountJson.get("percentage").getAsInt(),
                        discountJson.get("value").getAsInt()
                ),
                new Expiry(
                        expireJson.get("redeem_unlimited").getAsString().equals("true"),
                        expireJson.get("expire_never").getAsString().equals("true"),
                        expireJson.get("limit").getAsInt(),
                        StringUtil.toModernDate(expireJson.get("date").getAsString())
                ),
                BasketType.valueOf(jsonObject.get("basket_type").getAsString().toUpperCase()),
                StringUtil.toModernDate(jsonObject.get("start_date").getAsString()),
                jsonObject.get("user_limit").getAsInt(),
                jsonObject.get("minimum").getAsInt(),
                jsonObject.get("username").getAsString(),
                jsonObject.get("note").getAsString()
        );
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", effective=" + effective +
                ", discount=" + discount +
                ", expiry=" + expiry +
                ", basketType='" + basketType + '\'' +
                ", startDate=" + startDate +
                ", userLimit=" + userLimit +
                ", minimum=" + minimum +
                ", username='" + username + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
