package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Color;

import java.util.Optional;

public class CheckoutCmd implements CommandExecutor {
    private final BuycraftPlugin plugin;

    public CheckoutCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(final CommandContext args) throws CommandException {
        final Audience src = (Audience) args.cause().root();
        Optional<String> packageId = args.one(Parameter.string().key("package").build());

        try {
            Package packageById = plugin.getListingUpdateTask().getPackageById(Integer.parseInt(packageId.get()));
            plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, packageById.getId(), (ServerPlayer) src));
        } catch (Exception e) {
            src.sendMessage(Component.text("Could not find package with id " + packageId.get()).color(TextColor.color(Color.RED)));
        }

        return CommandResult.success();
    }
}
