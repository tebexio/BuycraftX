package net.buycraft.plugin.bungeecord;

import lombok.Getter;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.md_5.bungee.api.plugin.Plugin;

public class BuycraftPlugin extends Plugin {
    @Getter
    private ApiClient apiClient;
    @Getter
    private PlaceholderManager placeholderManager;
}
