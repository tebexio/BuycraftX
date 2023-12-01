package io.tebex.sdk.platform.config;

import dev.dejvokep.boostedyaml.YamlDocument;

/**
 * The ProxyPlatformConfig class holds the configuration for the Tebex SDK.
 */
public class ProxyPlatformConfig implements IPlatformConfig {
    private final int configVersion;
    private YamlDocument yamlDocument;

    private boolean verbose;
    private String secretKey;

    /**
     * Creates a PlatformConfig instance with the provided configuration version.
     *
     * @param configVersion The configuration version.
     */
    public ProxyPlatformConfig(int configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * Sets the secret key.
     *
     * @param secretKey The secret key.
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Sets the YAML document for this configuration.
     *
     * @param yamlDocument The YAML document.
     */
    public void setYamlDocument(YamlDocument yamlDocument) {
        this.yamlDocument = yamlDocument;
    }

    /**
     * Returns the configuration version.
     *
     * @return The configuration version.
     */
    @Override
    public int getConfigVersion() {
        return configVersion;
    }

    /**
     * Returns the secret key.
     *
     * @return The secret key.
     */
    @Override
    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Returns the YAML document for this configuration.
     *
     * @return The YAML document.
     */
    @Override
    public YamlDocument getYamlDocument() {
        return yamlDocument;
    }

    @Override
    public String toString() {
        return "ProxyPlatformConfig{" +
                "configVersion=" + configVersion +
                ", yamlDocument=" + yamlDocument +
                ", verbose=" + verbose +
                ", secretKey='" + secretKey + '\'' +
                '}';
    }
}
