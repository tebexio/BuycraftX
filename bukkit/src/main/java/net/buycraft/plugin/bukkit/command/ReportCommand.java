package net.buycraft.plugin.bukkit.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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

public class ReportCommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public ReportCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + plugin.getI18n().get("report_wait"));

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out, true);

        String date = new Date().toString();
        final String os = System.getProperty("os.name") + " | " + System.getProperty("os.version") + " | " + System.getProperty("os.arch");
        String javaVersion = System.getProperty("java.version") + " | " + System.getProperty("java.vendor");
        String serverVersion = Bukkit.getBukkitVersion();
        String serverIP = Bukkit.getIp();
        int serverPort = Bukkit.getPort();
        String buycraftVersion = plugin.getDescription().getVersion();

        writer.println("### Server Information ###");
        writer.println("Report generated on " + date);
        writer.println();

        writer.println("Operating system: " + os);
        writer.println("Java version: " + javaVersion);
        writer.println("Server version: " + serverVersion);
        writer.println("Server IP and port: " + serverIP + " / " + serverPort);
        writer.println("Online mode: " + Bukkit.getOnlineMode());
        writer.println("Buycraft is-bungeecord setting: " + plugin.getConfiguration().isBungeeCord());
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
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
                Path p = plugin.getDataFolder().toPath().resolve(filename);

                try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    w.write(out.toString());
                    sender.sendMessage(ChatColor.YELLOW + plugin.getI18n().get("report_saved", p.toAbsolutePath().toString()));
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("report_cant_save"));
                    plugin.getLogger().info(out.toString());
                }
            }
        });
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

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_report");
    }
}
