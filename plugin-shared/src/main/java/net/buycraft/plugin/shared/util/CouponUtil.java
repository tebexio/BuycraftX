package net.buycraft.plugin.shared.util;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.data.Coupon;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CouponUtil {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static long parseDuration(String text) {
        // Given a string like 1w3d4h5m, we will return a millisecond duration
        long result = 0;
        int numIdx = 0;
        for (int i = 0; i < text.length(); i++) {
            char at = text.charAt(i);
            if (at == 'd' || at == 'w' || at == 'm' || at == 'h') {
                String ns = text.substring(numIdx, i);
                numIdx = i + 1;

                if (ns.isEmpty()) {
                    continue;
                }

                int n = Integer.parseInt(ns);
                switch (at) {
                    case 'd':
                        result += TimeUnit.DAYS.toMillis(n);
                        break;
                    case 'w':
                        result += TimeUnit.DAYS.toMillis(n * 7);
                        break;
                    case 'h':
                        result += TimeUnit.HOURS.toMillis(n);
                        break;
                    case 'm':
                        result += TimeUnit.MINUTES.toMillis(n);
                        break;
                }
            } else if (!Character.isDigit(at)) {
                throw new IllegalArgumentException("Character " + at + " in position " + (i + 1) + " not valid");
            }
        }

        return result;
    }

    public static String generateCode() {
        char[] cs = new char[10];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length()));
        }
        return new String(cs);
    }

    public static Coupon parseArguments(String[] args) {
        Map<String, String> kv = new HashMap<>();

        String k = null;
        for (String arg : args) {
            if (k == null) {
                k = arg;
            } else {
                kv.put(k, arg);
                k = null;
            }
        }

        Coupon.CouponBuilder builder = Coupon.builder()
                .code(generateCode())
                .effective(new Coupon.Effective("cart", ImmutableList.<Integer>of(), ImmutableList.<Integer>of()))
                .basketType("both")
                .startDate(new Date());

        // Percentage / value discount
        String percentageStr = kv.get("percentage");
        String valueStr = kv.get("value");

        // One must be present, but not both.
        if (percentageStr == null && valueStr == null) {
            throw new IllegalArgumentException("percentage or value discount not found");
        }

        if (percentageStr != null && valueStr != null) {
            throw new IllegalArgumentException("percentage and value discounts are mutually exclusive");
        }

        if (percentageStr != null) {
            try {
                builder.discount(new Coupon.Discount("percentage", new BigDecimal(percentageStr), BigDecimal.ZERO));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("percentage is not valid (must be a number)");
            }
        } else {
            try {
                builder.discount(new Coupon.Discount("value", BigDecimal.ZERO, new BigDecimal(valueStr)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("value is not valid (must be a number)");
            }
        }

        // Expiry: none, limit of uses or expiry,
        String expiresStr = kv.get("expires");
        String limitStr = kv.get("limit");
        String neverExpire = "1";
        String unlimitedRedeem = "1";

        if (expiresStr != null) {
            neverExpire = "0";
            long ms = parseDuration(expiresStr);
            if (ms == 0) {
                throw new IllegalArgumentException("Invalid duration!");
            }
            builder.expire(new Coupon.Expire("timestamp", 0, new Date(System.currentTimeMillis() + ms)));
        }

        if (limitStr != null) {
            unlimitedRedeem = "0";
            try {
                builder.expire(new Coupon.Expire("limit", Integer.parseInt(limitStr), new Date()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("limit is not valid (must be a number)");
            }
        }

        if (limitStr == null && expiresStr == null) {
            builder.expire(new Coupon.Expire("timestamp", 0, new Date(System.currentTimeMillis() + 1)));
        }

        builder.expireNever(Integer.parseInt(neverExpire));
        builder.redeemUnlimited(Integer.parseInt(unlimitedRedeem));

        String minimumBasket = kv.get("min_value");
        if (minimumBasket != null) {
            try {
                builder.minimum(new BigDecimal(minimumBasket));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("min_value is not valid (must be a number)");
            }
        } else {
            builder.minimum(BigDecimal.ZERO);
        }

        String perUserUses = kv.get("user_limit");
        if (perUserUses != null) {
            try {
                builder.userLimit(Integer.parseInt(perUserUses));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("user_limit is not valid (must be a number)");
            }
        }

	String discountMethod = kv.get("discount_application_method");

        if (discountMethod == null) {
            discountMethod = "0";
        }
        if (discountMethod.equals("0") || discountMethod.equals("1") || discountMethod.equals("2")) {
            try {
                builder.discountMethod(Integer.parseInt(discountMethod));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("discount_application_method is not valid (must be 0, 1 or 2)");
            }
        } else {
            throw new IllegalArgumentException("discount_application_method must be 0, 1 or 2");
        }


        return builder.build();
    }
}
