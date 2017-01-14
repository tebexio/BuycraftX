package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.shared.util.ReportBuilder;
import net.buycraft.plugin.sponge.BuycraftPlugin;
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
        src.sendMessage(Text.builder(plugin.getI18n().get("report_wait")).color(TextColors.RED).build());

        plugin.getPlatform().executeAsync(() -> {
            String serverIP = (Sponge.getServer().getBoundAddress().isPresent()) ? Sponge.getServer().getBoundAddress().get().getHostName() : "?";
            int serverPort = (Sponge.getServer().getBoundAddress().isPresent()) ? Sponge.getServer().getBoundAddress().get().getPort() : -1;

            ReportBuilder builder = ReportBuilder.builder()
                    .client(plugin.getHttpClient())
                    .configuration(plugin.getConfiguration())
                    .platform(plugin.getPlatform())
                    .duePlayerFetcher(plugin.getDuePlayerFetcher())
                    .ip(serverIP)
                    .port(serverPort)
                    .listingUpdateTask(plugin.getListingUpdateTask())
                    .serverOnlineMode(Sponge.getServer().getOnlineMode())
                    .build();

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getBaseDirectory().resolve(filename);
            String generated = builder.generate();

            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(generated);
                src.sendMessage(Text.builder(plugin.getI18n().get("report_saved", p.toAbsolutePath().toString())).color(TextColors.YELLOW).build());
            } catch (IOException e) {
                src.sendMessage(Text.builder(plugin.getI18n().get("report_cant_save")).color(TextColors.RED).build());
                plugin.getLogger().info(generated);
            }
        });
        return CommandResult.success();
    }
}
