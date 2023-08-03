package io.tebex.sdk.placeholder;

import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.placeholder.defaults.NamePlaceholder;
import io.tebex.sdk.placeholder.defaults.UuidPlaceholder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PlaceholderManager {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[{\\(<\\[](name|player|username)[}\\)>\\]]", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNIQUE_ID_PATTERN = Pattern.compile("[{\\(<\\[](uuid|id)[}\\)>\\]]", Pattern.CASE_INSENSITIVE);

    private final List<Placeholder> placeholders;


    public PlaceholderManager() {
        this.placeholders = new ArrayList<>();
    }

    public void register(Placeholder placeholder) {
        if(this.placeholders.contains(placeholder))
            throw new IllegalArgumentException("Placeholder already registered");

        this.placeholders.add(placeholder);
    }

    public void registerDefaults() {
        register(new NamePlaceholder(this));
        register(new UuidPlaceholder(this));
    }

    public String handlePlaceholders(QueuedPlayer player, String command) {
        for (Placeholder placeholder : this.placeholders) {
            command = placeholder.handle(player, command);
        }

        return command;
    }

    public Pattern getUsernameRegex() {
        return USERNAME_PATTERN;
    }

    public Pattern getUniqueIdRegex() {
        return UNIQUE_ID_PATTERN;
    }
}
