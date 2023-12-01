package io.tebex.sdk.request.builder;

import io.tebex.sdk.obj.BasketType;
import io.tebex.sdk.obj.DiscountType;

import java.time.LocalDate;
import java.util.List;

public class CreateCouponRequest {
    public enum EffectiveOn {
        PACKAGE,
        CATEGORY,
        CART
    }

    public enum DiscountMethod {
        EACH_PACKAGE(0),
        BASKET_BEFORE_SALES(1),
        BASKET_AFTER_SALES(2);

        private final int value;

        DiscountMethod(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final String code;
    private final EffectiveOn effectiveOn;
    private final List<Integer> effectiveIds;
    private final DiscountType discountType;
    private final int discountValue;
    private final LocalDate startDate;

    private int minimum;
    private DiscountMethod discountMethod;
    private boolean redeemUnlimited;
    private boolean canExpire;
    private LocalDate expiryDate;
    private int expiryLimit;
    private final BasketType basketType;
    private String username;
    private String note;

    public CreateCouponRequest(String code, EffectiveOn effectiveOn, List<Integer> effectiveIds, DiscountType discountType, int discountValue, LocalDate startDate) {
        this.code = code;
        this.effectiveOn = effectiveOn;
        this.effectiveIds = effectiveIds;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;

        this.minimum = 0;
        this.discountMethod = DiscountMethod.EACH_PACKAGE;
        this.redeemUnlimited = true;
        this.basketType = BasketType.BOTH;
    }

    public String getCode() {
        return code;
    }

    public EffectiveOn getEffectiveOn() {
        return effectiveOn;
    }

    public List<Integer> getEffectiveIds() {
        return effectiveIds;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getMinimum() {
        return minimum;
    }

    public DiscountMethod getDiscountMethod() {
        return discountMethod;
    }

    public boolean canRedeemUnlimited() {
        return redeemUnlimited;
    }

    public boolean canExpire() {
        return canExpire;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getExpiryLimit() {
        return expiryLimit;
    }

    public BasketType getBasketType() {
        return basketType;
    }

    public String getUsername() {
        return username;
    }

    public String getNote() {
        return note;
    }

    // Setter methods
    public void setMinimumBasketValue(int minimumBasketValue) {
        this.minimum = minimumBasketValue;
    }

    public void setDiscountMethod(DiscountMethod discountMethod) {
        this.discountMethod = discountMethod;
    }

    public void setUnlimitedRedeems(boolean redeemUnlimited) {
        this.redeemUnlimited = redeemUnlimited;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setExpiryLimit(int expiryLimit) {
        this.expiryLimit = expiryLimit;
    }

    public void setCanExpire(boolean canExpire) {
        this.canExpire = canExpire;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
