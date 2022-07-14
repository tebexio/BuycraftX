package net.buycraft.plugin.sponge.command;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.util.Node;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.util.Color;

import java.io.IOException;
import java.util.Optional;

public class PackagesCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public PackagesCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(final CommandContext args) throws CommandException {
        final Audience src = (Audience) args.cause().root();
        Optional<Integer> packageId = args.one(Parameter.integerNumber().key("package").build());

        Node categories = new Node(plugin.getListingUpdateTask().getListing().getCategories(), ImmutableList.of(), plugin.getI18n().get("categories"), null);
        Optional<Category> category = categories.getSubcategories().stream().filter(categoryId -> categoryId.getId() == packageId.get()).findFirst();

        plugin.getListPackagesCmd().sendPaginatedMessage(categories.getChild(category.get()), src);

        return CommandResult.success();
    }
}
