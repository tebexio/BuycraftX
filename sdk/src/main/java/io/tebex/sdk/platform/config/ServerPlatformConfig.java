package io.tebex.sdk.platform.config;

import dev.dejvokep.boostedyaml.YamlDocument;

/**
 * The PlatformConfig class holds the configuration for the Tebex SDK.
 * It contains settings related to excluded players, minimum playtime, and various other options.
 */
public class ServerPlatformConfig implements IPlatformConfig {
    private final int configVersion;
    private YamlDocument yamlDocument;


    private String buyCommandName;
    private boolean buyCommandEnabled;
    private boolean checkForUpdates;
    private boolean verbose;
    private boolean proxyMode;
    private String secretKey;

    private boolean autoReportEnabled;

    /**
     * Creates a PlatformConfig instance with the provided configuration version.
     *
     * @param configVersion The configuration version.
     */
    public ServerPlatformConfig(int configVersion) {
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

    public void setBuyCommandName(String buyCommandName) {
        this.buyCommandName = buyCommandName;
    }

    public void setBuyCommandEnabled(boolean buyCommandEnabled) {
        this.buyCommandEnabled = buyCommandEnabled;
    }

    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setProxyMode(boolean proxyMode) {
        this.proxyMode = proxyMode;
    }

    public void setAutoReportEnabled(boolean autoReportEnabled) { this.autoReportEnabled = autoReportEnabled; }

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

    public String getBuyCommandName() {
        return buyCommandName;
    }

    public boolean isBuyCommandEnabled() {
        return buyCommandEnabled;
    }

    public boolean isCheckForUpdates() {
        return checkForUpdates;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    public boolean isProxyMode() {
        return proxyMode;
    }

    public boolean isAutoReportEnabled() { return autoReportEnabled; }

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
        return "ServerPlatformConfig{" +
                "configVersion=" + configVersion +
                ", yamlDocument=" + yamlDocument +
                ", buyCommandName='" + buyCommandName + '\'' +
                ", buyCommandEnabled=" + buyCommandEnabled +
                ", checkForUpdates=" + checkForUpdates +
                ", verbose=" + verbose +
                ", proxyMode=" + proxyMode +
                ", secretKey='" + secretKey + '\'' +
                ", autoReportEnabled=" + autoReportEnabled +
                '}';
    }
}
