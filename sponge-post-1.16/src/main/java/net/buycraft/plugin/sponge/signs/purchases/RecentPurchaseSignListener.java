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
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.buycraft.plugin.sponge.util.BlockUtil.isSign;

public class RecentPurchaseSignListener {
    private final BuycraftPlugin plugin;

    public RecentPurchaseSignListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onSignChange(ChangeSignEvent event, @First ServerPlayer player) {
        boolean ourSign;

        try {
            ourSign = Arrays.asList("[buycraft_rp]", "[tebex_rp]").contains(PlainTextComponentSerializer.plainText().serialize(event.text().get(0)).toLowerCase());
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!ourSign) {
            return;
        }

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
    public void onBlockBreak(ChangeBlockEvent.All event, @First ServerPlayer player) {
        event.transactions(Operations.BREAK.get()).filter(trans -> isSign(trans.original().state().type())).forEach(trans -> {
            Optional<ServerLocation> locationOptional = trans.original().location();
            if (locationOptional.isPresent() && !removeSign(player, SpongeSerializedBlockLocation.create(locationOptional.get()))) {
                trans.setValid(false);
            }
        });
    }
}
