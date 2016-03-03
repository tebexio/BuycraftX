package net.buycraft.plugin.sponge.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import okhttp3.Request;
import okhttp3.Response;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
public class ReportSubCommand implements CommandExecutor {
    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        sender.sendMessage(Text.builder("Please wait...").color(TextColors.GOLD).build());

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out, true);

        String date = new Date().toString();
        final String os = System.getProperty("os.name") + " | " + System.getProperty("os.version") + " | " + System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version") + " | " + System.getProperty("java.vendor");
        String serverVersion = Sponge.getPlatform().getImplementation().getName();
        String serverIP = "";//TODO
        int serverPort = 25565;//TODO
        String buycraftVersion = Sponge.getPluginManager().fromInstance(plugin).get().getVersion();

        writer.println("### Server Information ###");
        writer.println("Report generated on " + date);
        writer.println();

        writer.println("Operating system: " + os);
        writer.println("Java version: " + javaVersion);
        writer.println("Server version: " + serverVersion);
        writer.println("Server IP and port: " + serverIP + " / " + serverPort);
        writer.println("Online mode: " + Sponge.getServer().getOnlineMode());
        writer.println();

        writer.println("### Plugin Information ###");
        writer.println("Plugin version: " + buycraftVersion);
        writer.println("Platform: Bukkit");
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
        writer.println("Listing: ");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(plugin.getListingUpdateTask().getListing(), writer);

        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            writer.println();
            writer.println();
            writer.println("### Service status ###");

            // Try fetching test URLs
            tryGet("Buycraft plugin API", "https://plugin.buycraft.net", writer);
            tryGet("Google over HTTPS", "https://encrypted.google.com", writer);
            tryGet("Google over HTTP", "http://www.google.com", writer);

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getWorkFolder().resolve(filename);

            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(out.toString());
                sender.sendMessage(Text.builder("Report saved as " + p.toAbsolutePath().toString()).color(TextColors.GOLD).build());
            } catch (IOException e) {
                sender.sendMessage(Text.builder("Can't save report. Dumping onto console...").color(TextColors.RED).build());
                plugin.getLogger().info(out.toString());
            }
        });
        return CommandResult.success();
    }

    private void tryGet(String type, String url, PrintWriter writer) {
        Request request = new Request.Builder().get().url(url).build();

        try {
            Response response = plugin.getHttpClient().newCall(request).execute();
            writer.println("Can access " + type + " (" + url + ")");

            response.body().close();
        } catch (IOException e) {
            writer.println("Can't access " + type + " (" + url + "):");
            e.printStackTrace(writer);
        }
    }
}
