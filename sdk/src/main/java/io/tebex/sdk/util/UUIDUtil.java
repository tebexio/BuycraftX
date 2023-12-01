package io.tebex.sdk.util;

import java.util.UUID;

public class UUIDUtil {
    /**
     * Translates a Mojang-style UUID into an UUID Java can use. The Tebex plugin API returns all results with
     * Mojang-style UUIDs.
     *
     * @param id the Mojang UUID to use
     * @return the Java UUID
     */
    public static UUID mojangIdToJavaId(String id) {
        return UUID.fromString(id.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));
    }
}
