package net.buycraft.plugin.platform;

import lombok.NonNull;
import lombok.Value;

@Value
public class PlatformInformation {
    @NonNull
    private final PlatformType type;
    @NonNull
    private final String version;
}
