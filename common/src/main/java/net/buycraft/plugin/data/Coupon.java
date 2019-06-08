package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public final class Coupon {
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
    private final boolean expireNever;
    @SerializedName("redeem_unlimited")
    private final boolean redeemUnlimited;
    private final BigDecimal minimum;
    @SerializedName("username")
    private final String username;
    @SerializedName("note")
    private final String note;

    Coupon(final int id, final String code, final Effective effective, final Discount discount, final Expire expire, final String basketType, final Date startDate, final int userLimit, final int discountMethod, final boolean expireNever, final boolean redeemUnlimited, final BigDecimal minimum, final String username, final String note) {
        this.id = id;
        this.code = code;
        this.effective = effective;
        this.discount = discount;
        this.expire = expire;
        this.basketType = basketType;
        this.startDate = startDate;
        this.userLimit = userLimit;
        this.discountMethod = discountMethod;
        this.expireNever = expireNever;
        this.redeemUnlimited = redeemUnlimited;
        this.minimum = minimum;
        this.username = username;
        this.note = note;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public Effective getEffective() {
        return this.effective;
    }

    public Discount getDiscount() {
        return this.discount;
    }

    public Expire getExpire() {
        return this.expire;
    }

    public String getBasketType() {
        return this.basketType;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public int getUserLimit() {
        return this.userLimit;
    }

    public int getDiscountMethod() {
        return this.discountMethod;
    }

    public boolean getExpireNever() {
        return this.expireNever;
    }

    public boolean getRedeemUnlimited() {
        return this.redeemUnlimited;
    }

    public BigDecimal getMinimum() {
        return this.minimum;
    }

    public String getUsername() {
        return this.username;
    }

    public String getNote() {
        return this.note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coupon coupon = (Coupon) o;

        if (id != coupon.id) return false;
        if (userLimit != coupon.userLimit) return false;
        if (discountMethod != coupon.discountMethod) return false;
        if (expireNever != coupon.expireNever) return false;
        if (redeemUnlimited != coupon.redeemUnlimited) return false;
        if (!Objects.equals(code, coupon.code)) return false;
        if (!Objects.equals(effective, coupon.effective)) return false;
        if (!Objects.equals(discount, coupon.discount)) return false;
        if (!Objects.equals(expire, coupon.expire)) return false;
        if (!Objects.equals(basketType, coupon.basketType)) return false;
        if (!Objects.equals(startDate, coupon.startDate)) return false;
        if (!Objects.equals(minimum, coupon.minimum)) return false;
        if (!Objects.equals(username, coupon.username)) return false;
        return Objects.equals(note, coupon.note);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (effective != null ? effective.hashCode() : 0);
        result = 31 * result + (discount != null ? discount.hashCode() : 0);
        result = 31 * result + (expire != null ? expire.hashCode() : 0);
        result = 31 * result + (basketType != null ? basketType.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + userLimit;
        result = 31 * result + discountMethod;
        result = 31 * result + (expireNever ? 1 : 0);
        result = 31 * result + (redeemUnlimited ? 1 : 0);
        result = 31 * result + (minimum != null ? minimum.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Coupon(id=" + this.getId() + ", code=" + this.getCode() + ", effective=" + this.getEffective() + ", discount=" + this.getDiscount() + ", expire=" + this.getExpire() + ", basketType=" + this.getBasketType() + ", startDate=" + this.getStartDate() + ", userLimit=" + this.getUserLimit() + ", discountMethod=" + this.getDiscountMethod() + ", expireNever=" + this.getExpireNever() + ", redeemUnlimited=" + this.getRedeemUnlimited() + ", minimum=" + this.getMinimum() + ", username=" + this.getUsername() + ", note=" + this.getNote() + ")";
    }

    public static final class Effective {
        private final String type;
        private final List<Integer> packages;
        private final List<Integer> categories;

        public Effective(final String type, final List<Integer> packages, final List<Integer> categories) {
            this.type = type;
            this.packages = packages;
            this.categories = categories;
        }

        public String getType() {
            return this.type;
        }

        public List<Integer> getPackages() {
            return this.packages;
        }

        public List<Integer> getCategories() {
            return this.categories;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Effective effective = (Effective) o;

            if (!Objects.equals(type, effective.type)) return false;
            if (!Objects.equals(packages, effective.packages)) return false;
            return Objects.equals(categories, effective.categories);
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (packages != null ? packages.hashCode() : 0);
            result = 31 * result + (categories != null ? categories.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Coupon.Effective(type=" + this.getType() + ", packages=" + this.getPackages() + ", categories=" + this.getCategories() + ")";
        }
    }

    public static final class Discount {
        private final String type;
        private final BigDecimal percentage;
        private final BigDecimal value;

        public Discount(final String type, final BigDecimal percentage, final BigDecimal value) {
            this.type = type;
            this.percentage = percentage;
            this.value = value;
        }

        public String getType() {
            return this.type;
        }

        public BigDecimal getPercentage() {
            return this.percentage;
        }

        public BigDecimal getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Discount discount = (Discount) o;

            if (!Objects.equals(type, discount.type)) return false;
            if (!Objects.equals(percentage, discount.percentage)) return false;
            return Objects.equals(value, discount.value);
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Coupon.Discount(type=" + this.getType() + ", percentage=" + this.getPercentage() + ", value=" + this.getValue() + ")";
        }
    }

    public static final class Expire {
        private final String type;
        private final int limit;
        private final Date date;

        public Expire(final String type, final int limit, final Date date) {
            this.type = type;
            this.limit = limit;
            this.date = date;
        }

        public String getType() {
            return this.type;
        }

        public int getLimit() {
            return this.limit;
        }

        public Date getDate() {
            return this.date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Expire expire = (Expire) o;

            if (limit != expire.limit) return false;
            if (!Objects.equals(type, expire.type)) return false;
            return Objects.equals(date, expire.date);
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + limit;
            result = 31 * result + (date != null ? date.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Coupon.Expire(type=" + this.getType() + ", limit=" + this.getLimit() + ", date=" + this.getDate() + ")";
        }
    }

    public static class Builder {
        private int id;
        private String code;
        private Effective effective;
        private Discount discount;
        private Expire expire;
        private String basketType;
        private Date startDate;
        private int userLimit;
        private int discountMethod;
        private boolean expireNever;
        private boolean redeemUnlimited;
        private BigDecimal minimum;
        private String username;
        private String note;

        Builder() {
        }

        public Builder id(final int id) {
            this.id = id;
            return this;
        }

        public Builder code(final String code) {
            this.code = code;
            return this;
        }

        public Builder effective(final Effective effective) {
            this.effective = effective;
            return this;
        }

        public Builder discount(final Discount discount) {
            this.discount = discount;
            return this;
        }

        public Builder expire(final Expire expire) {
            this.expire = expire;
            return this;
        }

        public Builder basketType(final String basketType) {
            this.basketType = basketType;
            return this;
        }

        public Builder startDate(final Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder userLimit(final int userLimit) {
            this.userLimit = userLimit;
            return this;
        }

        public Builder discountMethod(final int discountMethod) {
            this.discountMethod = discountMethod;
            return this;
        }

        public Builder expireNever(final boolean expireNever) {
            this.expireNever = expireNever;
            return this;
        }

        public Builder expireNever(final int expireNever) {
            this.expireNever = expireNever == 1;
            return this;
        }

        public Builder redeemUnlimited(final boolean redeemUnlimited) {
            this.redeemUnlimited = redeemUnlimited;
            return this;
        }

        public Builder redeemUnlimited(final int redeemUnlimited) {
            this.redeemUnlimited = redeemUnlimited == 1;
            return this;
        }

        public Builder minimum(final BigDecimal minimum) {
            this.minimum = minimum;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder note(final String note) {
            this.note = note;
            return this;
        }

        public Coupon build() {
            return new Coupon(id, code, effective, discount, expire, basketType, startDate, userLimit, discountMethod, expireNever, redeemUnlimited, minimum, username, note);
        }

        @Override
        public String toString() {
            return "Coupon.Builder(id=" + this.id + ", code=" + this.code + ", effective=" + this.effective + ", discount=" + this.discount + ", expire=" + this.expire + ", basketType=" + this.basketType + ", startDate=" + this.startDate + ", userLimit=" + this.userLimit + ", discountMethod=" + this.discountMethod + ", expireNever=" + this.expireNever + ", redeemUnlimited=" + this.redeemUnlimited + ", minimum=" + this.minimum + ", username=" + this.username + ", note=" + this.note + ")";
        }
    }
}
