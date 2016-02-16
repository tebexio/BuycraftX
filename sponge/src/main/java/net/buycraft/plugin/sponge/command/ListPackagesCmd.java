package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by meyerzinn on 2/14/16.
 */
@AllArgsConstructor
public class ListPackagesCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationBuilder builder = paginationService.builder();

        List<Text> contents = new ArrayList<Text>();
        for (Category category : plugin.getListingUpdateTask().getListing().getCategories()) {
            for (final Package p : category.getPackages()) {
                contents.add(
                        Text.builder(p.getName()).color(TextColors
                                .AQUA)
                                .onClick(TextActions.executeCallback(new Consumer<CommandSource>() {
                                    @Override public void accept(CommandSource commandSource) {
                                        if (commandSource instanceof Player) {
                                            plugin.getPlatform()
                                                    .executeBlocking(new SendCheckoutLinkTask(plugin, p.getId(), (Player) commandSource));
                                        }
                                    }
                                }))
                                //                                    .onClick(TextActions.openUrl(new URL((plugin.getApiClient()
                                // .getCheckoutUri(((Player) src).getName(), p.getId())
                                //                                    ).getUrl())))
                                .append
                                        (Text.of(" "))
                                .append
                                        (Text.builder
                                                (plugin
                                                        .getServerInformation()
                                                        .getAccount()
                                                        .getCurrency()
                                                        .getSymbol() + p
                                                        .getEffectivePrice().toString()).color(TextColors.GRAY).build()).build());
            }
        }
        builder.title(Text.builder("Buycraft Packages").color(TextColors.AQUA).build()).contents(contents).paddingString("-").sendTo(src);
        return CommandResult.success();
    }
}
