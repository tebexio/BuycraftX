package net.buycraft.plugin.bukkit;

import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.bukkit.tasks.DuePlayerFetcher;
import net.buycraft.plugin.bukkit.tasks.ImmediateExecutionRunner;
import net.buycraft.plugin.bukkit.util.placeholder.NamePlaceholder;
import net.buycraft.plugin.bukkit.util.placeholder.PlaceholderManager;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.data.responses.ServerInformation;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public class BuycraftPlugin extends JavaPlugin {
    @Getter
    @Setter
    private ApiClient apiClient;
    @Getter
    private DuePlayerFetcher duePlayerFetcher;
    @Getter
    private final PlaceholderManager placeholderManager = new PlaceholderManager();
    @Getter
    private final BuycraftConfiguration configuration = new BuycraftConfiguration();
    @Getter
    private ServerInformation serverInformation;

    @Override
    public void onEnable() {
        // Initialize configuration.
        try {
            Path configPath = getDataFolder().toPath().resolve("config.properties");
            if (!configPath.toFile().exists()) {
                configuration.fillDefaults();
                configuration.save(configPath);
            } else {
                configuration.load(getDataFolder().toPath().resolve("config.properties"));
                configuration.fillDefaults();
            }
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Unable to load configuration! The plugin will disable itself now.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize API client.
        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using /buy secret.");
        } else {
            ApiClient client = new ProductionApiClient(configuration.getServerKey());
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().severe(String.format("We can't check your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());

        // Queueing tasks.
        getServer().getScheduler().runTaskLaterAsynchronously(this, duePlayerFetcher = new DuePlayerFetcher(this), 20);

        // Register listener.
        getServer().getPluginManager().registerEvents(new BuycraftListener(this), this);
    }

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();
    }
}
