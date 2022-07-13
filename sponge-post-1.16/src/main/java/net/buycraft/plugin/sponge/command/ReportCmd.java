package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.shared.util.ReportBuilder;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.util.Color;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public ReportCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        Audience src = (Audience) args.cause().root();

        src.sendMessage(Component.text(plugin.getI18n().get("report_wait")).color(TextColor.color(Color.RED)));

        plugin.getPlatform().executeAsync(() -> {
            String serverIP = (Sponge.server().boundAddress().isPresent()) ? Sponge.server().boundAddress().get().getHostName() : "?";
            int serverPort = (Sponge.server().boundAddress().isPresent()) ? Sponge.server().boundAddress().get().getPort() : -1;

            ReportBuilder builder = ReportBuilder.builder()
                    .client(plugin.getHttpClient())
                    .configuration(plugin.getConfiguration())
                    .platform(plugin.getPlatform())
                    .duePlayerFetcher(plugin.getDuePlayerFetcher())
                    .ip(serverIP)
                    .port(serverPort)
                    .listingUpdateTask(plugin.getListingUpdateTask())
                    .serverOnlineMode(Sponge.server().isOnlineModeEnabled())
                    .build();

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getBaseDirectory().resolve(filename);
            String generated = builder.generate();

            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(generated);
                src.sendMessage(Component.text(plugin.getI18n().get("report_saved", p.toAbsolutePath().toString())).color(TextColor.color(Color.YELLOW)));
            } catch (IOException e) {
                src.sendMessage(Component.text(plugin.getI18n().get("report_cant_save")).color(TextColor.color(Color.RED)));
                plugin.getLogger().info(generated);
            }
        });
        return CommandResult.success();
    }
}
