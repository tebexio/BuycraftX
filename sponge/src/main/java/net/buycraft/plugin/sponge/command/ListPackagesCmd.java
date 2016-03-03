package net.buycraft.plugin.sponge.command;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import net.buycraft.plugin.util.Node;
import net.buycraft.plugin.sponge.BuycraftPlugin;
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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by meyerzinn on 2/14/16.
 */
@AllArgsConstructor
public class ListPackagesCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (plugin.getApiClient() == null) {
            src.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
            return CommandResult.success();
        }

        if (plugin.getListingUpdateTask().getListing() == null) {
            src.sendMessage(Text.builder("We're currently retrieving the listing. Sit tight!").color(TextColors.RED).build());
            return CommandResult.success();
        }

        try {
            sendPaginatedMessage(new Node(plugin.getListingUpdateTask().getListing().getCategories(), new ArrayList<Package>(), "Categories", Optional
                    .absent()), src);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return CommandResult.success();
    }

    private void sendPaginatedMessage(Node node, CommandSource source) throws IOException, ApiException {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationBuilder builder = paginationService.builder();
        List<Text> contents = node.getSubcategories().stream()
                .map(category -> Text.builder("> " + category.getName()).color(TextColors.AQUA).onClick(TextActions.executeCallback(commandSource -> {
                    if (commandSource instanceof Player) {
                        try {
                            sendPaginatedMessage(node.getChild(category), source);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                })).build()).collect(Collectors.toList());
        for (Package p : node.getPackages()) {
            contents.add(Text.builder(p.getName()).color(TextColors.GRAY).append(Text.builder(" for $x.".replace("$", plugin.getApiClient()
                    .getServerInformation().getAccount().getCurrency().getSymbol()).replace("x", "" + p.getEffectivePrice())).color(TextColors
                    .BLUE).build()).onClick(TextActions.executeCallback(commandSource -> {
                if (commandSource instanceof Player) {
                    new SendCheckoutLinkTask(plugin, p.getId(), (Player) commandSource).run();
                }
            })).build());
        }
        builder.title(Text.builder("BuycraftX Listing").color(TextColors.AQUA).build()).contents(contents).paddingString("-").sendTo(source);
    }

}
