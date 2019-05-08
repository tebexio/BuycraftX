package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.shared.util.ReportBuilder;
import net.buycraft.plugin.velocity.BuycraftPlugin;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

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
    public void execute(final CommandSource sender, String[] args) {
        sender.sendMessage(TextComponent.of(plugin.getI18n().get("report_wait")).color(TextColor.YELLOW));

        plugin.getPlatform().executeAsync(() -> {
            InetSocketAddress listener = plugin.getServer().getBoundAddress();
            ReportBuilder builder = ReportBuilder.builder()
                    .client(plugin.getHttpClient())
                    .configuration(plugin.getConfiguration())
                    .platform(plugin.getPlatform())
                    .duePlayerFetcher(plugin.getDuePlayerFetcher())
                    .ip(listener.getAddress().toString())
                    .port(listener.getPort())
                    .serverOnlineMode(plugin.getServer().getConfiguration().isOnlineMode())
                    .build();

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getDataFolder().toPath().resolve(filename);
            String generated = builder.generate();
            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(generated);
                sender.sendMessage(TextComponent.of(plugin.getI18n().get("report_saved", p.toAbsolutePath().toString())).color(TextColor.YELLOW));
            } catch (IOException e) {
                sender.sendMessage(TextComponent.of(plugin.getI18n().get("report_cant_save")).color(TextColor.RED));
                plugin.getLogger().info(generated);
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_report");
    }
}
