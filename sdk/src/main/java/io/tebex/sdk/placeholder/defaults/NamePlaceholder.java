package io.tebex.sdk.placeholder.defaults;

import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.placeholder.Placeholder;
import io.tebex.sdk.placeholder.PlaceholderManager;

public class NamePlaceholder implements Placeholder {
    private final PlaceholderManager placeholderManager;

    public NamePlaceholder(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }

    @Override
    public String handle(QueuedPlayer player, String command) {
        return placeholderManager.getUsernameRegex().matcher(command).replaceAll(player.getName());
    }
}