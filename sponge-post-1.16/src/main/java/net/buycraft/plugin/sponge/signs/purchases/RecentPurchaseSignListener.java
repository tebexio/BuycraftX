package net.buycraft.plugin.sponge.signs.purchases;

import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.Optional;

public class RecentPurchaseSignListener {
    private final BuycraftPlugin plugin;

    public RecentPurchaseSignListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onSignChange(ChangeSignEvent event) {
        boolean ourSign;

        try {
            ourSign = Arrays.asList("[buycraft_rp]", "[tebex_rp]").contains(PlainTextComponentSerializer.plainText().serialize(event.originalText().get(0)).toLowerCase());
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!ourSign) {
            return;
        }

        Optional<ServerPlayer> pl = event.cause().first(ServerPlayer.class);
        if (!pl.isPresent()) {
            // This change was not caused by a player.
            return;
        }
        ServerPlayer player = pl.get();

        if (!player.hasPermission("buycraft.admin")) {
            event.cause().first(Player.class).get().sendMessage(Component.text("You can't create Buycraft signs.").color(TextColor.color(Color.RED)));
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(event.originalText().get(1)).toLowerCase());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            player.sendMessage(Component.text("The second line must be a number.").color(TextColor.color(Color.RED)));
            return;
        }
        if (pos <= 0) {
            player.sendMessage(Component.text("You can't show negative or zero purchases!").color(TextColor.color(Color.RED)));
            return;
        }
        if (pos > 100) {
            player.sendMessage(Component.text("You can't show more than 100 recent purchases!").color(TextColor.color(Color.RED)));
            return;
        }

        plugin.getRecentPurchaseSignStorage().addSign(new RecentPurchaseSignPosition(SpongeSerializedBlockLocation.create(event.sign().location()), pos));
        player.sendMessage(Component.text("Added new recent purchase sign!").color(TextColor.color(Color.GREEN)));

        // The below is due to the design of the Sponge Data API
        SignData signData = event.text();
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
        event.getTransactions().forEach(trans -> {
            if ((trans.getOriginal().getState().getType().equals(BlockTypes.WALL_SIGN) || trans.getOriginal().getState().getType().equals(BlockTypes.STANDING_SIGN))) {
                Optional<Location<World>> locationOptional = trans.getOriginal().getLocation();
                Optional<Player> playerOptional = event.getCause().first(Player.class);
                if (!removeSign(playerOptional.get(), SpongeSerializedBlockLocation.create(locationOptional.get()))) {
                    event.setCancelled(true);
                }
            }
        });
    }
}
