package net.buycraft.plugin.bukkit.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class ReportCommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public ReportCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Please wait...");

        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out, true);

        String date = new Date().toString();
        String os = System.getProperty("os.name") + " | " + System.getProperty("os.version") + " | " + System.getProperty("os.arch");
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
        writer.println();

        writer.println("### Plugin Information ###");
        writer.println("Plugin version: " + buycraftVersion);
        writer.println("Connected to Buycraft? " + (plugin.getApiClient() != null));
        writer.println("Web store information found? " + (plugin.getServerInformation() != null));
        if (plugin.getServerInformation() != null) {
            writer.println("Web store URL: " + plugin.getServerInformation().getAccount().getDomain());
            writer.println("Web store name: " + plugin.getServerInformation().getAccount().getName());
            writer.println("Web store currency: " + plugin.getServerInformation().getAccount().getCurrency().getIso4217());

            writer.println("Server name: " + plugin.getServerInformation().getServer().getName());
            writer.println("Server ID: " + plugin.getServerInformation().getServer().getId());
        }

        writer.println("Players in queue: " + plugin.getDuePlayerFetcher().getDuePlayers());
        writer.println("Listing update last completed: " + plugin.getListingUpdateTask().getLastUpdate());
        writer.println("Listing: ");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(plugin.getListingUpdateTask().getListing(), writer);
    }

    @Override
    public String getDescription() {
        return "Generates a report with debugging information you can send to support.";
    }
}
