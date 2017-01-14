package net.buycraft.plugin.bungeecord.command;

import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.shared.util.ReportBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
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

        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                InetSocketAddress listener1 = plugin.getProxy().getConfig().getListeners().iterator().next().getHost();
                ReportBuilder builder = ReportBuilder.builder()
                        .client(plugin.getHttpClient())
                        .configuration(plugin.getConfiguration())
                        .platform(plugin.getPlatform())
                        .duePlayerFetcher(plugin.getDuePlayerFetcher())
                        .ip(listener1.getAddress().toString())
                        .port(listener1.getPort())
                        .serverOnlineMode(plugin.getProxy().getConfig().isOnlineMode())
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
