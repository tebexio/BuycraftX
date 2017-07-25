package net.buycraft.plugin.sponge.signs.buynow;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.shared.config.signs.storage.SavedBuyNowSign;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SignUpdateApplication;
import net.buycraft.plugin.sponge.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.TargetBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
public class BuyNowSignListener {

    private final BuycraftPlugin plugin;
    private final Map<UUID, Long> signCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 250; // 5 ticks

    @Listener
    public void onSignChange(ChangeSignEvent event) {
        boolean ourSign;

        try {
            ourSign = event.getOriginalText().lines().get(0).toPlain().equalsIgnoreCase("[buycraft_buy]");
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



        plugin.getBuyNowSignStorage().addSign(new SavedBuyNowSign(
                SpongeSerializedBlockLocation.create(event.getTargetTile().getLocation()),
                pos));
        player.sendMessage(Text.builder("Added new buy now sign!").color(TextColors.GREEN).build());

        // The below is due to the design of the Sponge Data API
        SignData signData = event.getText();
        ListValue<Text> lines = signData.lines();
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                lines.set(i, Text.builder("Buy!").build());
            } else {
                lines.set(i, Text.EMPTY);
            }
        }
        signData.set(lines);

        plugin.getPlatform().executeAsync(new BuyNowSignUpdater(plugin));
    }

    private boolean isSign(Location<World> sign) {
        return sign.getBlockType().equals(BlockTypes.WALL_SIGN) || sign.getBlockType().equals(BlockTypes.STANDING_SIGN);
    }

    private boolean removeSign(Player player, SerializedBlockLocation location) {
        if (plugin.getBuyNowSignStorage().containsLocation(location)) {
            if (!player.hasPermission("buycraft.admin")) {
                player.sendMessage(Text.builder("You don't have permission to break this sign.").color(TextColors.RED).build());
                return false;
            }
            if (plugin.getBuyNowSignStorage().removeSign(location)) {
                player.sendMessage(Text.builder("Removed buy now sign!").color(TextColors.RED).build());
                return true;
            } else {
                player.sendMessage(Text.builder("Unable to remove buy now sign!").color(TextColors.RED).build());
                return false;
            }
        }
        return true;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {

        event.getTransactions().stream().forEach((trans) -> {
            if ((trans.getOriginal().getState().getType().equals(BlockTypes.WALL_SIGN) || trans.getOriginal().getState().getType().equals(BlockTypes.STANDING_SIGN))) {
                Optional<Location<World>> locationOptional = trans.getOriginal().getLocation();
                Optional<Player> playerOptional = event.getCause().first(Player.class);
                if (!removeSign(playerOptional.get(), SpongeSerializedBlockLocation.create(locationOptional.get()))) {
                    event.setCancelled(true);
                }

            }
        });
    }

    @Listener
    public void onRightClickBlock(InteractBlockEvent.Secondary event){

        BlockSnapshot b = event.getTargetBlock();

        if (!(b.getState().getType().equals(BlockTypes.WALL_SIGN) || b.getState().getType().equals(BlockTypes.STANDING_SIGN))) {
            return;
        }

        Player p = event.getCause().first(Player.class).get();


        SerializedBlockLocation sbl = SpongeSerializedBlockLocation.create(b.getLocation().get());

        for (SavedBuyNowSign s : plugin.getBuyNowSignStorage().getSigns()) {
            if (s.getLocation().equals(sbl)) {
                // Signs are rate limited (per player) in order to limit API calls issued.
                Long ts = signCooldowns.get(p.getUniqueId());
                long now = System.currentTimeMillis();
                if (ts == null || ts + COOLDOWN_MS <= now) {
                    signCooldowns.put(p.getUniqueId(), now);
                    plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, s.getPackageId(), p));
                }

                return;
            }
        }
    }
}
