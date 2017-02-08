package net.buycraft.plugin.shared.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class CouponUtilTest {
    @Test
    public void parseDurationEmptyDuration() {
        Assert.assertEquals(0, CouponUtil.parseDuration(""));
    }

    @Test
    public void parseDurationEmptyDuration2() {
        Assert.assertEquals(0, CouponUtil.parseDuration("dwhm"));
    }

    @Test
    public void parseDurationPartialDuration() {
        Assert.assertEquals(TimeUnit.DAYS.toMillis(8), CouponUtil.parseDuration("w7d24hm"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseDurationInvalidFormat() {
        Assert.assertEquals(TimeUnit.DAYS.toMillis(7), CouponUtil.parseDuration("xyz"));
    }
}