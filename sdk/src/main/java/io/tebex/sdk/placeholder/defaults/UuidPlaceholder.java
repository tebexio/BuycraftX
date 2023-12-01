package io.tebex.sdk.placeholder.defaults;

import io.tebex.sdk.obj.QueuedPlayer;
import io.tebex.sdk.placeholder.Placeholder;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.util.UUIDUtil;

public class UuidPlaceholder implements Placeholder {
    private final PlaceholderManager placeholderManager;

    public UuidPlaceholder(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }

    @Override
    public String handle(QueuedPlayer player, String command) {
        if (player.getUuid() == null) {
            return placeholderManager.getUsernameRegex().matcher(command).replaceAll(player.getName());
        }
        return placeholderManager.getUsernameRegex().matcher(command).replaceAll(UUIDUtil.mojangIdToJavaId(player.getUuid()).toString());
    }
}