package net.buycraft.plugin.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BuycraftConfiguration {
    private final Properties properties;

    public BuycraftConfiguration() {
        this.properties = new Properties();
    }

    private void defaultSet(String key, String value) {
        if (!properties.containsKey(key))
            properties.put(key, value);
    }

    public void load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    public void save(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, "BuycraftX configuration file");
        }
    }

    public void setServerKey(String key) {
        properties.setProperty("server-key", key);
    }

    public String getServerKey() {
        return properties.getProperty("server-key", null);
    }

    private boolean getBoolean(String key, boolean val) {
        if (!properties.containsKey(key))
            return val;

        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public boolean isBungeeCord() {
        return getBoolean("is-bungeecord", false);
    }

    public boolean isCheckForUpdates() {
        return getBoolean("check-for-updates", false);
    }

    public boolean isDisableBuyCommand() {
        return getBoolean("disable-buy-command", false);
    }

    public void fillDefaults() {
        defaultSet("server-key", "INVALID");
        defaultSet("is-bungeecord", "false");
        defaultSet("check-for-updates", "true");
        defaultSet("disable-buy-command", "false");
    }
}
