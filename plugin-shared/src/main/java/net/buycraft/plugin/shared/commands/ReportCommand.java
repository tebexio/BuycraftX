package net.buycraft.plugin.shared.commands;

import net.buycraft.plugin.shared.IBuycraftPlugin;
import net.buycraft.plugin.shared.util.ReportBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class ReportCommand implements BuycraftSubcommand {
    @Override
    public void execute(final IBuycraftPlugin plugin, final BuycraftCommandSender player, String[] args) {
        player.sendMessage(ChatColor.YELLOW, "report_wait");

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                ReportBuilder builder = ReportBuilder.builder()
                        .client(plugin.getHttpClient())
                        .configuration(plugin.getConfiguration())
                        .platform(plugin.getPlatform())
                        .duePlayerFetcher(plugin.getDuePlayerFetcher())
                        .ip(plugin.getAddress().getAddress().toString())
                        .port(plugin.getAddress().getPort())
                        .serverOnlineMode(plugin.isOnlineMode())
                        .listingUpdateTask(plugin.getListingUpdateTask())
                        .build();

                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                String filename = "report-" + f.format(new Date()) + ".txt";
                Path p = plugin.getBasePath().resolve(filename);
                String generated = builder.generate();

                try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    w.write(generated);
                    player.sendMessage(ChatColor.YELLOW, "report_saved", p.toAbsolutePath().toString());
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED, "report_cant_save");
                    plugin.getPlatform().log(Level.INFO, generated);
                }
            }
        });
    }

    @Override
    public String getDescriptionMessageName() {
        return "usage_report";
    }
}
