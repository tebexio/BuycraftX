package net.buycraft.plugin.sponge.signs.purchases;

import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.server.ServerLocation;

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
            ourSign = Arrays.asList("[buycraft_rp]", "[tebex_rp]").contains(PlainTextComponentSerializer.plainText().serialize(event.text().get(0)).toLowerCase());
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
            player.sendMessage(Component.text("You can't create Buycraft signs.").color(TextColor.color(Color.RED)));
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(event.text().get(1)).toLowerCase());
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

        plugin.getRecentPurchaseSignStorage().addSign(new RecentPurchaseSignPosition(SpongeSerializedBlockLocation.create((ServerLocation) event.sign().location()), pos));
        player.sendMessage(Component.text("Added new recent purchase sign!").color(TextColor.color(Color.GREEN)));

        ListValue.Mutable<Component> signText = event.text();
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                signText.set(i, Component.text("Buy!"));
            } else {
                signText.set(i, Component.empty());
            }
        }
        event.sign().offer(signText);

        plugin.getPlatform().executeAsync(new SignUpdater(plugin));
    }

    private boolean isSign(BlockType blockType) {
        return Arrays.asList(
                BlockTypes.ACACIA_WALL_SIGN, BlockTypes.BIRCH_WALL_SIGN, BlockTypes.DARK_OAK_WALL_SIGN, BlockTypes.JUNGLE_WALL_SIGN, BlockTypes.OAK_WALL_SIGN, BlockTypes.SPRUCE_WALL_SIGN,
                BlockTypes.ACACIA_SIGN, BlockTypes.BIRCH_SIGN, BlockTypes.DARK_OAK_SIGN, BlockTypes.JUNGLE_SIGN, BlockTypes.OAK_SIGN, BlockTypes.SPRUCE_SIGN
        ).contains(blockType);
    }

    private boolean removeSign(ServerPlayer player, SerializedBlockLocation location) {
        if (plugin.getRecentPurchaseSignStorage().containsLocation(location)) {
            if (!player.hasPermission("buycraft.admin")) {
                player.sendMessage(Component.text("You don't have permission to break this sign.").color(TextColor.color(Color.RED)));
                return false;
            }
            if (plugin.getRecentPurchaseSignStorage().removeSign(location)) {
                player.sendMessage(Component.text("Removed recent purchase sign!").color(TextColor.color(Color.RED)));
                return true;
            }
        }
        return true;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.All event) {
        event.transactions().forEach(trans -> {
            if (trans.operation() != Operations.BREAK.get()) return;
            if (isSign(trans.original().state().type())) {
                Optional<ServerLocation> locationOptional = trans.original().location();
                Optional<ServerPlayer> playerOptional = event.cause().first(ServerPlayer.class);
                if (! removeSign(playerOptional.get(), SpongeSerializedBlockLocation.create(locationOptional.get()))) {
                    event.setCancelled(true);
                }
            }
        });
    }
}
