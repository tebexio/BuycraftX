package net.buycraft.plugin.sponge;

import com.google.inject.Inject;
import io.keen.client.java.KeenClient;
import lombok.Getter;
import lombok.Setter;
import net.buycraft.plugin.IBuycraftPlatform;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.config.BuycraftConfiguration;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.execution.DuePlayerFetcher;
import net.buycraft.plugin.execution.placeholder.PlaceholderManager;
import net.buycraft.plugin.execution.strategy.CommandExecutor;
import net.buycraft.plugin.sponge.gui.CategoryViewGUI;
import net.buycraft.plugin.sponge.gui.ViewCategoriesGUI;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignListener;
import net.buycraft.plugin.sponge.signs.buynow.BuyNowSignStorage;
import net.buycraft.plugin.sponge.signs.purchases.RecentPurchaseSignStorage;
import net.buycraft.plugin.sponge.tasks.ListingUpdateTask;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Created by meyerzinn on 2/14/16.
 */
@Plugin(id = "buycraft-sponge", name = "Buycraft", version = "0.0.1-ALPHA")
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
    private ListingUpdateTask listingUpdateTask;
    @Getter
    private ServerInformation serverInformation;
    @Getter
    private CategoryViewGUI categoryViewGUI;
    @Getter
    private ViewCategoriesGUI viewCategoriesGUI;
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
    private Path configDir;

    @Listener
    public void onGamePreInitializationEvent(GamePreInitializationEvent event) {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    private CommandSpec buildCommands() {
        CommandSpec report =
                CommandSpec.builder().description(Text.of("Generates a report with debugging information you can send to support.")).build();
        return CommandSpec.builder().description(Text.of("Main command for the Buycraft plugin.")).build();
    }

}
