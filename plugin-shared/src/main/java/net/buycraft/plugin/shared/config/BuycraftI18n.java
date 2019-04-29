package net.buycraft.plugin.shared.config;

import com.google.common.base.Joiner;

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
    private ResourceBundle bundle;
    private ResourceBundle userBundle;

    public BuycraftI18n(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle("buycraftx_messages", locale,
                    BuycraftI18n.class.getClassLoader());
        } catch (Exception e) {
            new RuntimeException("Failed to load i18n files! Will be using message ids as a replacement", e).printStackTrace();
        }
    }

    public void loadUserBundle(Path resource) throws IOException {
        try (Reader reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
            userBundle = new PropertyResourceBundle(reader);
        }
    }

    public String get(String message, Object... params) {
        try {
            return MessageFormat.format(getBundleFor(message).getString(message), params);
        } catch (Exception e) {
            return "i18n:" + message + "(" + Joiner.on(", ").join(params) + ")";
        }
    }

    public ResourceBundle getBundleFor(String message) {
        return userBundle != null && userBundle.containsKey(message) ? userBundle : bundle;
    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }

    public ResourceBundle getUserBundle() {
        return this.userBundle;
    }
}
