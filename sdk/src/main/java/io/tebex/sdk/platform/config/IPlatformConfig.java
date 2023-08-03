package io.tebex.sdk.platform.config;

import dev.dejvokep.boostedyaml.YamlDocument;

/**
 * The base PlatformConfig class holds the configuration for the Tebex SDK.
 */
public interface IPlatformConfig {
    int getConfigVersion();
    String getSecretKey();
    boolean isVerbose();
    YamlDocument getYamlDocument();
}
