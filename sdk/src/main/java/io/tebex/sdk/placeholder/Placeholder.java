package io.tebex.sdk.placeholder;

import io.tebex.sdk.obj.QueuedPlayer;

public interface Placeholder {
    String handle(QueuedPlayer player, String command);
}