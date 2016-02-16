package net.buycraft.plugin.sponge;

import com.google.inject.Inject;
import io.keen.client.java.KeenClient;
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
import net.buycraft.plugin.sponge.command.ListPackagesCmd;
import net.buycraft.plugin.sponge.command.RefreshCmd;
import net.buycraft.plugin.sponge.command.ReportCmd;
import net.buycraft.plugin.sponge.command.SecretCmd;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignStorage;
import net.buycraft.plugin.sponge.signs.purchases.RecentPurchaseSignStorage;
import net.buycraft.plugin.sponge.tasks.ListingUpdateTask;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Created by meyerzinn on 2/14/16.
 */
@Plugin(id = "buycraft", name = "Buycraft", version = BuycraftPlugin.MAGIC_VERSION)
public class BuycraftPlugin {

    static final String MAGIC_VERSION = "SET_BY_MAGIC";

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
    private KeenClient keenClient;
    @Getter
    private RecentPurchaseSignStorage recentPurchaseSignStorage;
    @Getter
    private OkHttpClient httpClient;
    @Getter
    private BuyNowSignStorage buyNowSignStorage;
    @Getter
    private BuyNowSignListener buyNowSignListener;
    @Getter
    private IBuycraftPlatform platform;
    @Getter
    private CommandExecutor commandExecutor;

    @Getter
    @Inject
    private Logger logger;

    @Getter
    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path config;

    @Listener
    public void onGamePreInitializationEvent(GamePreInitializationEvent event) {
        platform = new SpongeBuycraftPlatform(this);
        try {
            if (!getConfig().toFile().exists()) {
                configuration.fillDefaults();
                configuration.save(getConfig());
            } else {
                configuration.load(getConfig());
                configuration.fillDefaults();
            }
        } catch (IOException e) {
            getLogger().error("Unable to load configuration! The plugin will disable itself now.", e);
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
                getLogger().error(String.format("We can't check if your server can connect to Buycraft: %s", e.getMessage()));
            }
            apiClient = client;
        }
        placeholderManager.addPlaceholder(new NamePlaceholder());
        placeholderManager.addPlaceholder(new UuidPlaceholder());
        platform.executeAsyncLater(duePlayerFetcher = new DuePlayerFetcher(platform), 1, TimeUnit.SECONDS);
        commandExecutor = new QueuedCommandExecutor(platform);
        Sponge.getScheduler().createTaskBuilder().intervalTicks(1).delayTicks(1).execute((Runnable) commandExecutor).async().submit(this);
        listingUpdateTask = new ListingUpdateTask(this);
        if (apiClient != null) {
            getLogger().info("Fetching all server packages...");
            listingUpdateTask.run();
            Sponge.getScheduler().createTaskBuilder().delayTicks(20 * 60 * 20).intervalTicks(20 * 60 * 20).execute(listingUpdateTask).async()
                    .submit(this);
        }

        Sponge.getEventManager().registerListeners(this, new BuycraftListener(this));

        /*
        *TODO: Commands.
         */
        Sponge.getCommandManager().register(this, buildCommands(), "buycraft", "buy");


    }

    private CommandSpec buildCommands() {
        CommandSpec refresh =
                CommandSpec.builder()
                        .description(Text.of("Refreshes the package listing."))
                        .permission("buycraft.admin")
                        .executor(new RefreshCmd(this))
                        .build();
        CommandSpec secret =
                CommandSpec.builder()
                        .description(Text.of("Sets the secret key to use for this server."))
                        .permission("buycraft.admin")
                        .arguments(GenericArguments.string(Text.of("secret")))
                        .executor(new SecretCmd(this))
                        .build();
        CommandSpec report =
                CommandSpec.builder()
                        .description(Text.of("Generates a report with debugging information you can send to support."))
                        .executor(new ReportCmd(this))
                        .permission("buycraft.admin")
                        .build();
        CommandSpec list = CommandSpec.builder()
                .description(Text.of("Lists all Buycraft packages and their prices."))
                .executor(new ListPackagesCmd(this))
                .build();
        return CommandSpec.builder()
                .description(Text.of("Main command for the Buycraft plugin."))
                .child(report, "report")
                .child(list, "list", "packages")
                .child(secret, "secret")
                .child(refresh, "refresh")
                .build();
    }

    public void saveConfiguration() throws IOException {
        configuration.save(getConfig());
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
