package net.buycraft.plugin.sponge;

import com.bugsnag.Client;
import com.google.gson.Gson;
import com.google.inject.Inject;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenJsonHandler;
import io.keen.client.java.KeenProject;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.NamePlaceholder;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.placeholder.UuidPlaceholder;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.execution.strategy.QueuedCommandExecutor;
import net.buycraft.plugin.sponge.command.*;
import net.buycraft.plugin.sponge.logging.BugsnagNilLogger;
import net.buycraft.plugin.sponge.tasks.ListingUpdateTask;
import net.buycraft.plugin.sponge.util.KeenUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(id = "buycraftx", name = "BuycraftX", version = "0.1.1-SNAPSHOT")
public class BuycraftPlugin {
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
    @Getter
    private KeenClient keenClient;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;
    @Inject
    @Getter
    private Logger logger;
    @Inject
    @Getter
    @ConfigDir(sharedRoot = false)
    private Path workFolder;
    @Getter
    private boolean running = true;
    @Getter
    private ListingUpdateTask listingUpdateTask;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        platform = new SpongeBuycraftPlatform(this);

        try {
            Files.createDirectories(workFolder);

            Path configPath = workFolder.resolve("config.properties");
            if (!configPath.toFile().exists()) {
                configuration.fillDefaults();
                configuration.save(configPath);
            } else {
                configuration.load(workFolder.resolve("config.properties"));
                configuration.fillDefaults();
            }
        } catch (IOException e) {
            getLogger().error("Unable to load configuration!", e);
            running = false;
            return;
        }

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        String serverKey = configuration.getServerKey();
        if (serverKey == null || serverKey.equals("INVALID")) {
            getLogger().info("Looks like this is a fresh setup. Get started by using 'buycraft secret <key>' in the console.");
        } else {
            getLogger().info("Validating your server key...");
            ApiClient client = new ProductionApiClient(configuration.getServerKey(), httpClient);
            try {
                updateInformation(client);
            } catch (IOException | ApiException e) {
                getLogger().warn(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }

        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());

        platform.executeAsyncLater(
                duePlayerFetcher = new DuePlayerFetcher(platform),
                20,
                TimeUnit.MILLISECONDS
        );

        commandExecutor = new QueuedCommandExecutor(platform);

        Sponge.getScheduler().createTaskBuilder()
                .execute((Runnable) commandExecutor)
                .intervalTicks(1)
                .delayTicks(1)
                .submit(this);

        listingUpdateTask = new ListingUpdateTask(this);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run();

            Sponge.getScheduler().createTaskBuilder()
                    .execute(listingUpdateTask)
                    .delay(10, TimeUnit.SECONDS)
                    .interval(10, TimeUnit.SECONDS)
                    .async()
                    .submit(this);
        }
    }

    @Listener
    public void onGamePostInitialization(GamePostInitializationEvent event) {
        if (!running) return;

        Sponge.getEventManager().registerListeners(this, new BuycraftListener(this));

        CommandSpec command = CommandSpec.builder()
                .description(Text.of("Main command for the Buycraft plugin."))
                .child(
                        CommandSpec.builder()
                                .description(Text.of("Forces a purchase check."))
                                .executor(new ForceCheckSubcommand(this))
                                .build(),
                        "forcecheck"
                ).child(
                        CommandSpec.builder()
                                .description(Text.of("Sets the secret key to use for this server."))
                                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("key"))))
                                .executor(new SecretSubcommand(this))
                                .build(),
                        "secret"
                ).child(
                        CommandSpec.builder()
                                .description(Text.of("Retrieves public information about the webstore this server is associated with."))
                                .executor(new InformationSubcommand(this))
                                .build(),
                        "info"
                ).child(
                        CommandSpec.builder()
                                .description(Text.of("Refreshes the list of categories and packages."))
                                .executor(new RefreshSubcommand(this))
                                .build(),
                        "refresh"
                ).child(
                        CommandSpec.builder()
                                .description(Text.of("Forces an update to your recent purchase signs."))
                                .executor(new SignUpdateSubcommand(this))
                                .build(),
                        "signupdate"
                ).child(
                        CommandSpec.builder()
                                .description(Text.of("Generates a report with debugging information you can send to support."))
                                .executor(new ReportSubCommand(this))
                                .build(),
                        "report"
                ).build();

        Sponge.getCommandManager().register(this, command, "buycraft");

        if (serverInformation != null) {
            /**
             * This duplicated. {@see net.buycraft.plugin.bukkit.BuycraftPlugin:180}
             */
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
            KeenProject project = new KeenProject(serverInformation.getAnalytics().getInternal().getProject(),
                    serverInformation.getAnalytics().getInternal().getKey(),
                    null);
            keenClient.setDefaultProject(project);

            Sponge.getScheduler().createTaskBuilder()
                    .execute(new Runnable() {
                        @Override
                        public void run() {
                            KeenUtils.postServerInformation(BuycraftPlugin.this);
                        }
                    })
                    .interval(20 * TimeUnit.DAYS.toSeconds(1), TimeUnit.MILLISECONDS)
                    .async()
                    .submit(this);

        }

        // Set up Bugsnag.
        Client bugsnagClient = new Client("cac4ea0fdbe89b5004d8ab8d5409e594", false);
        bugsnagClient.setLogger(new BugsnagNilLogger());
        //We can't do this in Sponge!
//        getLogger().addHandler(new BugsnagGlobalLoggingHandler(bugsnagClient, this));
//        getLogger().addHandler(new BugsnagLoggingHandler(bugsnagClient, this));
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        try {
            saveConfiguration();
        } catch (IOException e) {
            getLogger().warn("Can't save configuration", e);
        }
    }

    public void saveConfiguration() throws IOException {
        Path configPath = workFolder.resolve("config.properties");
        configuration.save(configPath);
    }

    public void updateInformation(ApiClient client) throws IOException, ApiException {
        serverInformation = client.getServerInformation();

        if (!configuration.isBungeeCord() && Sponge.getServer().getOnlineMode() != serverInformation.getAccount().isOnlineMode()) {
            getLogger().warn("Your server and webstore online mode settings are mismatched. Unless you are using" +
                    " a proxy and server combination (such as BungeeCord/Spigot or LilyPad/Connect) that corrects UUIDs, then" +
                    " you may experience issues with packages not applying.");
            getLogger().warn("If you are sure you have understood and verified that this has been set up, set " +
                    "is-bungeecord=true in your BuycraftX config.properties.");
        }
    }
}
