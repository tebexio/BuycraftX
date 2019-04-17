package net.buycraft.plugin.nukkit.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import net.buycraft.plugin.nukkit.BuycraftPlugin;
import net.buycraft.plugin.shared.util.ReportBuilder;

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
        sender.sendMessage(TextFormat.YELLOW + plugin.getI18n().get("report_wait"));

        plugin.getPlatform().executeAsync(() -> {
            ReportBuilder builder = ReportBuilder.builder()
                    .client(plugin.getHttpClient())
                    .configuration(plugin.getConfiguration())
                    .platform(plugin.getPlatform())
                    .duePlayerFetcher(plugin.getDuePlayerFetcher())
                    .ip(plugin.getServer().getIp())
                    .port(plugin.getServer().getPort())
                    .serverOnlineMode(false)
                    .build();

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getDataFolder().toPath().resolve(filename);
            String generated = builder.generate();

            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(generated);
                sender.sendMessage(TextFormat.YELLOW + plugin.getI18n().get("report_saved", p.toAbsolutePath().toString()));
            } catch (IOException e) {
                sender.sendMessage(TextFormat.RED + plugin.getI18n().get("report_cant_save"));
                plugin.getLogger().info(generated);
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_report");
    }
}
