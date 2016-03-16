package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor
public class ReportCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(final CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.builder("Please wait...").color(TextColors.RED).build());

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out, true);

        String date = new Date().toString();
        final String os = System.getProperty("os.name") + " | " + System.getProperty("os.version") + " | " + System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version") + " | " + System.getProperty("java.vendor");
        String serverPlatform = Sponge.getPlatform().getImplementation().getName();

        String serverVersion = Sponge.getPlatform().getImplementation().getVersion().orElse("UNKNOWN");
        String serverIP = (Sponge.getServer().getBoundAddress().isPresent()) ? Sponge.getServer().getBoundAddress().get().getHostName() : "?";
        int serverPort = (Sponge.getServer().getBoundAddress().isPresent()) ? Sponge.getServer().getBoundAddress().get().getPort() : -1;
        String buycraftVersion = "Unknown.";
        for (Field f : plugin.getClass().getFields()) {
            Plugin plugin = f.getAnnotation(Plugin.class);
            if (plugin != null) {
                buycraftVersion = plugin.version();
            }
        }

        writer.println("### Server Information ###");
        writer.println("Report generated on " + date);
        writer.println();

        writer.println("Operating system: " + os);
        writer.println("Java version: " + javaVersion);
        writer.println("Server version: " + serverVersion);
        writer.println("Server IP and port: " + serverIP + " / " + serverPort);
        writer.println("Online mode: " + Sponge.getServer().getOnlineMode());
        writer.println("Buycraft is-bungeecord setting: " + plugin.getConfiguration().isBungeeCord());
        writer.println();

        writer.println("### Plugin Information ###");
        writer.println("Plugin version: " + buycraftVersion);
        writer.println("Platform: " + serverPlatform);
        writer.println();
        writer.println("Connected to Buycraft? " + (plugin.getApiClient() != null));
        writer.println("Web store information found? " + (plugin.getServerInformation() != null));
        if (plugin.getServerInformation() != null) {
            writer.println("Web store ID: " + plugin.getServerInformation().getAccount().getId());
            writer.println("Web store URL: " + plugin.getServerInformation().getAccount().getDomain());
            writer.println("Web store name: " + plugin.getServerInformation().getAccount().getName());
            writer.println("Web store currency: " + plugin.getServerInformation().getAccount().getCurrency().getIso4217());
            writer.println("Web store in online mode? " + plugin.getServerInformation().getAccount().isOnlineMode());

            writer.println("Server name: " + plugin.getServerInformation().getServer().getName());
            writer.println("Server ID: " + plugin.getServerInformation().getServer().getId());
        }

        writer.println("Players in queue: " + plugin.getDuePlayerFetcher().getDuePlayers());
        writer.println("Listing update last completed: " + plugin.getListingUpdateTask().getLastUpdate());

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                writer.println();
                writer.println("### Service status ###");

                // Try fetching test URLs
                tryGet("Buycraft plugin API", "https://plugin.buycraft.net", writer);
                tryGet("Google over HTTPS", "https://encrypted.google.com", writer);
                tryGet("Google over HTTP", "http://www.google.com", writer);

                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                String filename = "report-" + f.format(new Date()) + ".txt";
                Path p = plugin.getBaseDirectory().resolve(filename);

                try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    w.write(out.toString());
                    src.sendMessage(Text.builder("Report saved as " + p.toAbsolutePath().toString()).color(TextColors.RED).build());
                } catch (IOException e) {
                    src.sendMessage(Text.builder("Can't save report. Dumping onto console...").color(TextColors.RED).build());
                    plugin.getLogger().info(out.toString());
                }
            }
        });
        return CommandResult.success();
    }

    private void tryGet(String type, String url, PrintWriter writer) {
        Request request = new Request.Builder()
                .get()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(url)
                .build();
        try {
            Response response = plugin.getHttpClient().newCall(request).execute();
            writer.println("Can access " + type + " (" + url + ")");

            // close the body!
            response.body().close();
        } catch (IOException e) {
            writer.println("Can't access " + type + " (" + url + "):");
            e.printStackTrace(writer);
        }
    }
}
