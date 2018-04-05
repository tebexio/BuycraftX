package net.buycraft.plugin.sponge.command;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.shared.util.Node;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ListPackagesCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
        if (plugin.getApiClient() == null) {
            sender.sendMessage(Text.builder(plugin.getI18n().get("generic_api_operation_error")).color(TextColors.RED).build());
            return CommandResult.success();
        }

        if (plugin.getListingUpdateTask().getListing() == null) {
            sender.sendMessage(Text.builder("We're currently retrieving the listing. Sit tight!").color(TextColors.RED).build());
            return CommandResult.success();
        }

        sendPaginatedMessage(new Node(plugin.getListingUpdateTask().getListing().getCategories(), ImmutableList.of(),
                plugin.getI18n().get("categories"), null), sender);

        return CommandResult.success();
    }

    private void sendPaginatedMessage(Node node, CommandSource source) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();
        List<Text> contents = node.getSubcategories().stream()
                .map(category -> Text.builder("> " + category.getName()).color(TextColors.GRAY).onClick(TextActions.executeCallback(commandSource -> {
                    if (commandSource instanceof Player) {
                        sendPaginatedMessage(node.getChild(category), source);
                    }
                })).build()).collect(Collectors.toList());
        for (Package p : node.getPackages()) {
            contents.add(Text.builder(p.getName()).color(TextColors.WHITE).append(Text.builder(" - ").color(TextColors.GRAY).build())
                    .append(Text.builder("$x".replace("$", plugin.getServerInformation().getAccount().getCurrency().getSymbol())
                            .replace("x", "" + p.getEffectivePrice())).color(TextColors.GREEN).build())
                    .onClick(TextActions.executeCallback(commandSource -> {
                        if (commandSource instanceof Player) {
                            plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, p.getId(), (Player) commandSource));
                        }
                    })).build());
        }
        builder.title(Text.builder(plugin.getI18n().get("sponge_listing")).color(TextColors.AQUA).build()).contents(contents).padding(Text.of("-")).sendTo(source);
    }

}
