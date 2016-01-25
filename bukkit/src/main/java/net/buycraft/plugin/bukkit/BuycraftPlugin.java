package net.buycraft.plugin.bukkit;

import com.google.gson.Gson;
import io.keen.client.java.*;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.bukkit.command.ForceCheckSubcommand;
import net.buycraft.plugin.bukkit.command.InformationSubcommand;
import net.buycraft.plugin.bukkit.command.RefreshSubcommand;
import net.buycraft.plugin.bukkit.command.SecretSubcommand;
import net.buycraft.plugin.bukkit.gui.CategoryViewGUI;
import net.buycraft.plugin.bukkit.gui.GUIUtil;
import net.buycraft.plugin.bukkit.gui.ViewCategoriesGUI;
import net.buycraft.plugin.bukkit.tasks.DuePlayerFetcher;
import net.buycraft.plugin.bukkit.tasks.ListingUpdateTask;
import net.buycraft.plugin.bukkit.util.KeenUtils;
import net.buycraft.plugin.bukkit.util.placeholder.NamePlaceholder;
import net.buycraft.plugin.bukkit.util.placeholder.PlaceholderManager;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.data.responses.ServerInformation;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private ListingUpdateTask listingUpdateTask;
    @Getter
    private ServerInformation serverInformation;
    @Getter
    private CategoryViewGUI categoryViewGUI;
    @Getter
    private ViewCategoriesGUI viewCategoriesGUI;
    @Getter
    private KeenClient keenClient;

    @Override
    public void onEnable() {
        // Pre-initialization.
        GUIUtil.setPlugin(this);

        // Initialize configuration.
        getDataFolder().mkdir();
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
            getLogger().info("Validating your server key...");
            ApiClient client = new ProductionApiClient(configuration.getServerKey());
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().severe(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }

        // Initialize placeholders.
        placeholderManager.addPlaceholder(new NamePlaceholder());

        // Queueing tasks.
        getServer().getScheduler().runTaskLaterAsynchronously(this, duePlayerFetcher = new DuePlayerFetcher(this), 20);
        listingUpdateTask = new ListingUpdateTask(this);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run(); // for a first synchronous run
            getServer().getScheduler().runTaskTimerAsynchronously(this, listingUpdateTask, 20 * 60 * 10, 20 * 60 * 10);
        }

        // Register listener.
        getServer().getPluginManager().registerEvents(new BuycraftListener(this), this);

        // Initialize and register commands.
        BuycraftCommand command = new BuycraftCommand();
        command.getSubcommandMap().put("forcecheck", new ForceCheckSubcommand(this));
        command.getSubcommandMap().put("secret", new SecretSubcommand(this));
        command.getSubcommandMap().put("info", new InformationSubcommand(this));
        command.getSubcommandMap().put("refresh", new RefreshSubcommand(this));
        getCommand("buycraft").setExecutor(command);

        // Initialize GUIs.
        viewCategoriesGUI = new ViewCategoriesGUI(this);
        viewCategoriesGUI.update();

        categoryViewGUI = new CategoryViewGUI(this);
        categoryViewGUI.update();

        // Send data to Keen IO
        if (serverInformation != null) {
            keenClient = new KeenClient.Builder() {
                @Override
                protected KeenJsonHandler getDefaultJsonHandler() throws Exception {
                    return new KeenJsonHandler() {
                        private final Gson gson = new Gson();

                        @Override
                        public Map<String, Object> readJson(Reader reader) throws IOException {
                            return gson.fromJson(reader, Map.class);
                        }

                        @Override
                        public void writeJson(Writer writer, Map<String, ?> map) throws IOException {
                            gson.toJson(map, writer);
                            writer.close();
                        }
                    };
                }
            }.build();
            KeenLogging.enableLogging();
            KeenProject project = new KeenProject(serverInformation.getAnalytics().getInternal().getProject(),
                    serverInformation.getAnalytics().getInternal().getKey(),
                    null);
            keenClient.setDefaultProject(project);

            getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    KeenUtils.postServerInformation(BuycraftPlugin.this);
                }
            }, 0, 20 * TimeUnit.DAYS.toSeconds(1));
        }
    }

    public void saveConfiguration() throws IOException {
        Path configPath = getDataFolder().toPath().resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();
    }
}
