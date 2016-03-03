package net.buycraft.plugin.sponge.command;

import lombok.AllArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@AllArgsConstructor
public class GuiCmd implements CommandExecutor {

    private final BuycraftPlugin plugin;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        src.sendMessage(Text.builder("Unfortunately, Sponge does not yet support custom inventories. This will be implemented when this feature is "
                + "added.").color(TextColors.RED).build());
        return CommandResult.success();
//        if (plugin.getApiClient() == null) {
//            src.sendMessage(Text.builder("Set up a secret key first with /buycraft secret.").color(TextColors.RED).build());
//            return CommandResult.success();
//        }
//
//        if (plugin.getListingUpdateTask().getListing() == null) {
//            src.sendMessage(Text.builder("We're currently retrieving the listing. Sit tight!").color(TextColors.RED).build());
//            return CommandResult.success();
//        }
//
//        if (src instanceof Player) {
//            GuiView view = new GuiView(plugin,
//                    new Node(plugin.getListingUpdateTask().getListing().getCategories(), new ArrayList<>(), "Categories", Optional.absent()),
//                    (Player) src);
//            view.open();
//        } else {
//            src.sendMessage(Text.builder("Only players can use this command!").color(TextColors.RED).build());
//        }
//        return CommandResult.success();
    }
}
