package io.tebex.sdk.platform;

import io.tebex.sdk.Tebex;

/**
 * The PlatformModule class represents a module that can be integrated into a server platform.
 * It provides base methods to manage the lifecycle of the module, such as enabling or disabling it.
 */
public abstract class PlatformModule {
    /**
     * Retrieves the name of the module.
     *
     * @return The name of the module.
     */
    public abstract String getName();

    /**
     * Called when the module is enabled.
     */
    public abstract void onEnable();

    /**
     * Called when the module is disabled.
     */
    public abstract void onDisable();

    /**
     * Retrieves the current platform instance.
     *
     * @return The current platform instance.
     */
    public Platform getPlatform() {
        return Tebex.get();
    }

    /**
     * Retrieves the required plugin for the module.
     * Override this method if the module depends on another plugin.
     *
     * @return The required plugin name, or null if no plugin is required.
     */
    public String getRequiredPlugin() {
        return null;
    }
}