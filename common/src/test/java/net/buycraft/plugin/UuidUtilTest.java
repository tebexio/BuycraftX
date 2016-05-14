package net.buycraft.plugin;

import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UuidUtilTest {
    private static final String MOJANG_UUID = "652a2bc4e8cd405db7b698156ee2dc09";
    private static final UUID JAVA_UUID = UUID.fromString("652a2bc4-e8cd-405d-b7b6-98156ee2dc09");

    @Test
    public void testMojangUuidToJavaUuid() throws Exception {
        Assert.assertEquals(JAVA_UUID, UuidUtil.mojangUuidToJavaUuid(MOJANG_UUID));
    }

    @Test(expected = NullPointerException.class)
    public void testMojangUuidToJavaUuid_NullId() throws Exception {
        UuidUtil.mojangUuidToJavaUuid(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMojangUuidToJavaUuid_InvalidRegex1() throws Exception {
        UuidUtil.mojangUuidToJavaUuid("42");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMojangUuidToJavaUuid_InvalidRegex2() throws Exception {
        UuidUtil.mojangUuidToJavaUuid(Strings.repeat("!", 32));
    }
}
