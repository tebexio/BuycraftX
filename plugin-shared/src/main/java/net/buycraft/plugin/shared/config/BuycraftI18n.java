package net.buycraft.plugin.shared.config;

import lombok.Getter;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class BuycraftI18n {
    @Getter
    private ResourceBundle bundle;
    @Getter
    private ResourceBundle userBundle;

    public BuycraftI18n(Locale locale) {
        bundle = ResourceBundle.getBundle("buycraftx_messages", locale);
    }

    public void loadUserBundle(Path resource) throws IOException {
        try (Reader reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
            userBundle = new PropertyResourceBundle(reader);
        }
    }

    public String get(String message, Object... params) {
        return MessageFormat.format(getBundleFor(message).getString(message), params);
    }

    public ResourceBundle getBundleFor(String message) {
        return userBundle != null && userBundle.containsKey(message) ? userBundle : bundle;
    }
}
