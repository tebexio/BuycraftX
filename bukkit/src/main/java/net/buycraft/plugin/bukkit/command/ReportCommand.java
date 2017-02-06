package net.buycraft.plugin.bukkit.command;

import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.shared.util.ReportBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedWriter;
import java.io.IOException;
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

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ReportBuilder builder = ReportBuilder.builder()
                        .client(plugin.getHttpClient())
                        .configuration(plugin.getConfiguration())
                        .platform(plugin.getPlatform())
                        .duePlayerFetcher(plugin.getDuePlayerFetcher())
                        .ip(Bukkit.getIp())
                        .port(Bukkit.getPort())
                        .listingUpdateTask(plugin.getListingUpdateTask())
                        .serverOnlineMode(Bukkit.getOnlineMode())
                        .build();

                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                String filename = "report-" + f.format(new Date()) + ".txt";
                Path p = plugin.getDataFolder().toPath().resolve(filename);
                String generated = builder.generate();

                try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    w.write(generated);
                    sender.sendMessage(ChatColor.YELLOW + plugin.getI18n().get("report_saved", p.toAbsolutePath().toString()));
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("report_cant_save"));
                    plugin.getLogger().info(generated);
                }
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_report");
    }
}
