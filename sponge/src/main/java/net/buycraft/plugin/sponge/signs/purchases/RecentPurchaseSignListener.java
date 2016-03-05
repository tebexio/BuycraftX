package net.buycraft.plugin.sponge.signs.purchases;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SignUpdateApplication;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import net.buycraft.plugin.sponge.util.SerializedBlockLocation;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;


@RequiredArgsConstructor
public class RecentPurchaseSignListener {

    private final BuycraftPlugin plugin;

    @Listener
    public void onSignChange(ChangeSignEvent event) {
        boolean ourSign;

        try {
            ourSign = event.getOriginalText().lines().get(0).toPlain().equalsIgnoreCase("[buycraft_rp]");
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!ourSign) {
            return;
        }

        Optional<Player> pl = event.getCause().first(Player.class);

        if (!pl.isPresent()) {
            // This change was not caused by a player.
            return;
        }

        Player player = pl.get();

        if (!player.hasPermission("buycraft.admin")) {
            event.getCause().first(Player.class).get().sendMessage(Text.builder("You can't create Buycraft signs.").color(TextColors.RED).build());
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(event.getOriginalText().lines().get(1).toPlain());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getCause().first(Player.class).get().sendMessage(Text.builder("The second line must be a number.").color(TextColors.RED).build());
            return;
        }
        if (pos <= 0) {
            player.sendMessage(Text.builder("You can't show negative or zero purchases!").color(TextColors.RED).build());
            return;
        }

        if (pos > 100) {
            player.sendMessage(Text.builder("You can't show more than 100 recent purchases!").color(TextColors.RED).build());
            return;
        }

        plugin.getRecentPurchaseSignStorage().addSign(new RecentPurchaseSignPosition(
                SerializedBlockLocation.fromSpongeLocation(event.getTargetTile().getLocation()), pos));
        player.sendMessage(Text.builder("Added new recent purchase sign!").color(TextColors.GREEN).build());

        // The below is due to the design of the Sponge Data API
        SignData signData = event.getText();
        ListValue<Text> lines = signData.lines();
        for (int i = 0; i < 4; i++) {
            lines.set(i, Text.EMPTY);
        }
        signData.set(lines);

        plugin.getPlatform().executeAsync(new SignUpdater(plugin));
    }

    private boolean isSign(Location<World> sign) {
        return sign.getBlockType().equals(BlockTypes.WALL_SIGN) || sign.getBlockType().equals(BlockTypes.STANDING_SIGN);
    }

    private boolean removeSign(Player player, SerializedBlockLocation location) {
        if (plugin.getRecentPurchaseSignStorage().containsLocation(location)) {
            if (!player.hasPermission("buycraft.admin")) {
                player.sendMessage(Text.builder("You don't have permission to break this sign.").color(TextColors.RED).build());
                return false;
            }
            if (plugin.getRecentPurchaseSignStorage().removeSign(location)) {
                player.sendMessage(Text.builder("Removed recent purchase sign!").color(TextColors.RED).build());
                return true;
            }
        }
        return true;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        // TODO: Check directions too
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Optional<Location<World>> locationOptional = transaction.getOriginal().getLocation();
            Optional<Player> playerOptional = event.getCause().first(Player.class);
            if (locationOptional.isPresent() && playerOptional.isPresent()) {
                Location<World> location = locationOptional.get();
                if (isSign(location)) {
                    if (!removeSign(playerOptional.get(), SerializedBlockLocation.fromSpongeLocation(location))) {
                        event.setCancelled(true);
                    }
                } else {
                    for (Direction direction : SignUpdateApplication.SKULL_CHECK) {
                        Location<World> rel = location.getRelative(direction);
                        if (isSign(rel)) {
                            if (!removeSign(playerOptional.get(), SerializedBlockLocation.fromSpongeLocation(rel))) {
                                event.setCancelled(true);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}
