package net.buycraft.plugin.shared.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class BuycraftConfiguration {
    private final Properties properties;

    public BuycraftConfiguration() {
        this.properties = new Properties();
    }

    private static String join(String separator, Collection<String> elements) {
        StringBuilder builder = new StringBuilder();
        for (String element : elements) {
            builder.append(element).append(separator);
        }
        builder.delete(builder.length() - separator.length(), builder.length());
        return builder.toString();
    }

    private void defaultSet(String key, String value) {
        if (properties.getProperty(key) == null)
            properties.setProperty(key, value);
    }

    public void load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    public void save(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(writer, "BuycraftX configuration file");
        }
    }

    public String getServerKey() {
        return properties.getProperty("server-key", null);
    }

    public void setServerKey(String key) {
        properties.setProperty("server-key", key);
    }

    public List<String> getBuyCommandName() {
        return Arrays.asList(properties.getProperty("buy-command-name", "buy").split(","));
    }

    public void setBuyCommandName(List<String> keys) {
        properties.setProperty("buy-command-name", join(",", keys));
    }

    public boolean isVerbose() {
        return getBoolean("verbose", true);
    }

    public void setVerbose(boolean verbose) {
        properties.setProperty("verbose", Boolean.toString(verbose));
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
        return getBoolean("check-for-updates", true);
    }

    public boolean isDisableBuyCommand() {
        return getBoolean("disable-buy-command", false);
    }

    private Locale getLocale() {
        return Locale.forLanguageTag(properties.getProperty("language", "en_US"));
    }

    public BuycraftI18n createI18n() {
        return new BuycraftI18n(getLocale());
    }

    public void fillDefaults() {
        defaultSet("server-key", "INVALID");
        defaultSet("is-bungeecord", "false");
        defaultSet("check-for-updates", "true");
        defaultSet("disable-buy-command", "false");
        defaultSet("buy-command-name", "buy");
        defaultSet("language", Locale.getDefault().toLanguageTag());
        defaultSet("verbose", "true");
    }
}
