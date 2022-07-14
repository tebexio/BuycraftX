package net.buycraft.plugin.sponge.command;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.shared.util.Node;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.util.Color;

import java.util.List;
import java.util.stream.Collectors;

public class ListPackagesCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public ListPackagesCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        Audience sender = (Audience) args.cause().root();

        if (plugin.getApiClient() == null) {
            sender.sendMessage(Component.text(plugin.getI18n().get("generic_api_operation_error")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        if (plugin.getListingUpdateTask().getListing() == null) {
            sender.sendMessage(Component.text("We're currently retrieving the listing. Sit tight!").color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        sendPaginatedMessage(new Node(plugin.getListingUpdateTask().getListing().getCategories(), ImmutableList.of(),
                plugin.getI18n().get("categories"), null), sender);

        return CommandResult.success();
    }

    private void sendPaginatedMessage(Node node, Audience source) {
        PaginationService paginationService = Sponge.server().serviceProvider().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();
        List<Component> contents = node.getSubcategories().stream()
                .map(category -> Component.text("> " + category.getName()).color(TextColor.color(Color.GRAY))
                        .onClick(TextActions.executeCallback(commandSource -> {
                    if (commandSource instanceof ServerPlayer) {
                        sendPaginatedMessage(node.getChild(category), source);
                    }
                })).build()).collect(Collectors.toList());
        for (Package p : node.getPackages()) {
            contents.add(Component.text(p.getName()).color(TextColor.color(Color.WHITE)).append(Component.text(" - ").color(TextColor.color(Color.GRAY)))
                    .append(Component.text("$x".replace("$", plugin.getServerInformation().getAccount().getCurrency().getSymbol())
                            .replace("x", "" + p.getEffectivePrice())).color(TextColor.color(Color.GREEN)))
                    .onClick(TextActions.executeCallback(commandSource -> {
                        if (commandSource instanceof ServerPlayer) {
                            plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, p.getId(), (ServerPlayer) commandSource));
                        }
                    })).build());
        }
        builder.title(Component.text(plugin.getI18n().get("sponge_listing")).color(TextColor.color(Color.BLUE))).contents(contents).padding(Component.text("-")).sendTo(source);
    }
}
