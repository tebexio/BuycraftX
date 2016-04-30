package net.buycraft.plugin.config;

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
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public void loadUserBundle(Path resource) throws IOException {
        try (Reader reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
            userBundle = new PropertyResourceBundle(reader);
        }
    }

    public String get(String message, Object... params) {
        if (userBundle != null && userBundle.containsKey(message)) {
            return MessageFormat.format(userBundle.getString(message), params);
        } else {
            return MessageFormat.format(bundle.getString(message), params);
        }
    }
}
