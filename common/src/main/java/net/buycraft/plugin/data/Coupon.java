package net.buycraft.plugin.data;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    private final int expireNever;
    @SerializedName("redeem_unlimited")
    private final int redeemUnlimited;
    private final BigDecimal minimum;
    @SerializedName("username")
    private final String username;
    @SerializedName("note")
    private final String note;

    Coupon(final int id, final String code, final Effective effective, final Discount discount, final Expire expire, final String basketType, final Date startDate, final int userLimit, final int discountMethod, final int expireNever, final int redeemUnlimited, final BigDecimal minimum, final String username, final String note) {
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

    public static CouponBuilder builder() {
        return new CouponBuilder();
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

    public int getExpireNever() {
        return this.expireNever;
    }

    public int getRedeemUnlimited() {
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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Coupon)) return false;
        final Coupon other = (Coupon) o;
        if (this.getId() != other.getId()) return false;
        final java.lang.Object this$code = this.getCode();
        final java.lang.Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final java.lang.Object this$effective = this.getEffective();
        final java.lang.Object other$effective = other.getEffective();
        if (this$effective == null ? other$effective != null : !this$effective.equals(other$effective)) return false;
        final java.lang.Object this$discount = this.getDiscount();
        final java.lang.Object other$discount = other.getDiscount();
        if (this$discount == null ? other$discount != null : !this$discount.equals(other$discount)) return false;
        final java.lang.Object this$expire = this.getExpire();
        final java.lang.Object other$expire = other.getExpire();
        if (this$expire == null ? other$expire != null : !this$expire.equals(other$expire)) return false;
        final java.lang.Object this$basketType = this.getBasketType();
        final java.lang.Object other$basketType = other.getBasketType();
        if (this$basketType == null ? other$basketType != null : !this$basketType.equals(other$basketType))
            return false;
        final java.lang.Object this$startDate = this.getStartDate();
        final java.lang.Object other$startDate = other.getStartDate();
        if (this$startDate == null ? other$startDate != null : !this$startDate.equals(other$startDate)) return false;
        if (this.getUserLimit() != other.getUserLimit()) return false;
        if (this.getDiscountMethod() != other.getDiscountMethod()) return false;
        if (this.getExpireNever() != other.getExpireNever()) return false;
        if (this.getRedeemUnlimited() != other.getRedeemUnlimited()) return false;
        final java.lang.Object this$minimum = this.getMinimum();
        final java.lang.Object other$minimum = other.getMinimum();
        if (this$minimum == null ? other$minimum != null : !this$minimum.equals(other$minimum)) return false;
        final java.lang.Object this$username = this.getUsername();
        final java.lang.Object other$username = other.getUsername();
        if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
        final java.lang.Object this$note = this.getNote();
        final java.lang.Object other$note = other.getNote();
        if (this$note == null ? other$note != null : !this$note.equals(other$note)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final java.lang.Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final java.lang.Object $effective = this.getEffective();
        result = result * PRIME + ($effective == null ? 43 : $effective.hashCode());
        final java.lang.Object $discount = this.getDiscount();
        result = result * PRIME + ($discount == null ? 43 : $discount.hashCode());
        final java.lang.Object $expire = this.getExpire();
        result = result * PRIME + ($expire == null ? 43 : $expire.hashCode());
        final java.lang.Object $basketType = this.getBasketType();
        result = result * PRIME + ($basketType == null ? 43 : $basketType.hashCode());
        final java.lang.Object $startDate = this.getStartDate();
        result = result * PRIME + ($startDate == null ? 43 : $startDate.hashCode());
        result = result * PRIME + this.getUserLimit();
        result = result * PRIME + this.getDiscountMethod();
        result = result * PRIME + this.getExpireNever();
        result = result * PRIME + this.getRedeemUnlimited();
        final java.lang.Object $minimum = this.getMinimum();
        result = result * PRIME + ($minimum == null ? 43 : $minimum.hashCode());
        final java.lang.Object $username = this.getUsername();
        result = result * PRIME + ($username == null ? 43 : $username.hashCode());
        final java.lang.Object $note = this.getNote();
        result = result * PRIME + ($note == null ? 43 : $note.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
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
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Coupon.Effective)) return false;
            final Coupon.Effective other = (Coupon.Effective) o;
            final java.lang.Object this$type = this.getType();
            final java.lang.Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
            final java.lang.Object this$packages = this.getPackages();
            final java.lang.Object other$packages = other.getPackages();
            if (this$packages == null ? other$packages != null : !this$packages.equals(other$packages)) return false;
            final java.lang.Object this$categories = this.getCategories();
            final java.lang.Object other$categories = other.getCategories();
            if (this$categories == null ? other$categories != null : !this$categories.equals(other$categories))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            final java.lang.Object $packages = this.getPackages();
            result = result * PRIME + ($packages == null ? 43 : $packages.hashCode());
            final java.lang.Object $categories = this.getCategories();
            result = result * PRIME + ($categories == null ? 43 : $categories.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
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
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Coupon.Discount)) return false;
            final Coupon.Discount other = (Coupon.Discount) o;
            final java.lang.Object this$type = this.getType();
            final java.lang.Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
            final java.lang.Object this$percentage = this.getPercentage();
            final java.lang.Object other$percentage = other.getPercentage();
            if (this$percentage == null ? other$percentage != null : !this$percentage.equals(other$percentage))
                return false;
            final java.lang.Object this$value = this.getValue();
            final java.lang.Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            final java.lang.Object $percentage = this.getPercentage();
            result = result * PRIME + ($percentage == null ? 43 : $percentage.hashCode());
            final java.lang.Object $value = this.getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
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
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof Coupon.Expire)) return false;
            final Coupon.Expire other = (Coupon.Expire) o;
            final java.lang.Object this$type = this.getType();
            final java.lang.Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
            if (this.getLimit() != other.getLimit()) return false;
            final java.lang.Object this$date = this.getDate();
            final java.lang.Object other$date = other.getDate();
            if (this$date == null ? other$date != null : !this$date.equals(other$date)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            result = result * PRIME + this.getLimit();
            final java.lang.Object $date = this.getDate();
            result = result * PRIME + ($date == null ? 43 : $date.hashCode());
            return result;
        }

        @Override
        public java.lang.String toString() {
            return "Coupon.Expire(type=" + this.getType() + ", limit=" + this.getLimit() + ", date=" + this.getDate() + ")";
        }
    }

    public static class CouponBuilder {
        private int id;
        private String code;
        private Effective effective;
        private Discount discount;
        private Expire expire;
        private String basketType;
        private Date startDate;
        private int userLimit;
        private int discountMethod;
        private int expireNever;
        private int redeemUnlimited;
        private BigDecimal minimum;
        private String username;
        private String note;

        CouponBuilder() {
        }

        public CouponBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public CouponBuilder code(final String code) {
            this.code = code;
            return this;
        }

        public CouponBuilder effective(final Effective effective) {
            this.effective = effective;
            return this;
        }

        public CouponBuilder discount(final Discount discount) {
            this.discount = discount;
            return this;
        }

        public CouponBuilder expire(final Expire expire) {
            this.expire = expire;
            return this;
        }

        public CouponBuilder basketType(final String basketType) {
            this.basketType = basketType;
            return this;
        }

        public CouponBuilder startDate(final Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public CouponBuilder userLimit(final int userLimit) {
            this.userLimit = userLimit;
            return this;
        }

        public CouponBuilder discountMethod(final int discountMethod) {
            this.discountMethod = discountMethod;
            return this;
        }

        public CouponBuilder expireNever(final int expireNever) {
            this.expireNever = expireNever;
            return this;
        }

        public CouponBuilder redeemUnlimited(final int redeemUnlimited) {
            this.redeemUnlimited = redeemUnlimited;
            return this;
        }

        public CouponBuilder minimum(final BigDecimal minimum) {
            this.minimum = minimum;
            return this;
        }

        public CouponBuilder username(final String username) {
            this.username = username;
            return this;
        }

        public CouponBuilder note(final String note) {
            this.note = note;
            return this;
        }

        public Coupon build() {
            return new Coupon(id, code, effective, discount, expire, basketType, startDate, userLimit, discountMethod, expireNever, redeemUnlimited, minimum, username, note);
        }

        @Override
        public java.lang.String toString() {
            return "Coupon.CouponBuilder(id=" + this.id + ", code=" + this.code + ", effective=" + this.effective + ", discount=" + this.discount + ", expire=" + this.expire + ", basketType=" + this.basketType + ", startDate=" + this.startDate + ", userLimit=" + this.userLimit + ", discountMethod=" + this.discountMethod + ", expireNever=" + this.expireNever + ", redeemUnlimited=" + this.redeemUnlimited + ", minimum=" + this.minimum + ", username=" + this.username + ", note=" + this.note + ")";
        }
    }
}
