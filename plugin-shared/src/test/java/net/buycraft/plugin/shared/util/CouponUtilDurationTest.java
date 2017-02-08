package net.buycraft.plugin.shared.util;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class CouponUtilDurationTest {
    public CouponUtilDurationTest(String string, long expected) {
        this.string = string;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "3m", TimeUnit.MINUTES.toMillis(3) },
                { "30h", TimeUnit.HOURS.toMillis(30) },
                { "30d", TimeUnit.DAYS.toMillis(30) },
                { "30m", TimeUnit.MINUTES.toMillis(30) },
                { "30w", TimeUnit.DAYS.toMillis(30*7) },
                { "48h30m", TimeUnit.HOURS.toMillis(48) + TimeUnit.MINUTES.toMillis(30) },
                { "1w7d24h60m", TimeUnit.DAYS.toMillis(15) + TimeUnit.HOURS.toMillis(1) },
                { "60m7d24h1w", TimeUnit.DAYS.toMillis(15) + TimeUnit.HOURS.toMillis(1) }
        });
    }

    private final String string;
    private final long expected;

    @org.junit.Test
    public void parseDuration() throws Exception {
        Assert.assertEquals(expected, CouponUtil.parseDuration(string));
    }

}