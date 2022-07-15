package net.buycraft.plugin.sponge.signs.buynow;

import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.shared.config.signs.storage.SavedBuyNowSign;
import net.buycraft.plugin.shared.config.signs.storage.SerializedBlockLocation;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.BuyNowSignUpdater;
import net.buycraft.plugin.sponge.tasks.SendCheckoutLinkTask;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.*;

import static net.buycraft.plugin.sponge.util.BlockUtil.isSign;

public class BuyNowSignListener {
    private static final long COOLDOWN_MS = 250; // 5 ticks
    private final BuycraftPlugin plugin;
    private final Map<UUID, Long> signCooldowns = new HashMap<>();

    public BuyNowSignListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onSignChange(ChangeSignEvent event, @First ServerPlayer player) {
        boolean ourSign;

        try {
            ourSign = Arrays.asList("[buycraft_buy]", "[tebex_buy]").contains(PlainTextComponentSerializer.plainText().serialize(event.text().get(0)).toLowerCase());
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

        plugin.getBuyNowSignStorage().addSign(new SavedBuyNowSign(SpongeSerializedBlockLocation.create((ServerLocation) event.sign().location()), pos));
        player.sendMessage(Component.text("Added new buy now sign!").color(TextColor.color(Color.GREEN)));

        ListValue.Mutable<Component> signText = event.text();
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                signText.set(i, Component.text("Buy!"));
            } else {
                signText.set(i, Component.empty());
            }
        }
        event.sign().offer(signText);

        plugin.getPlatform().executeAsync(new BuyNowSignUpdater(plugin));
    }

    private boolean removeSign(ServerPlayer player, SerializedBlockLocation location) {
        if (plugin.getBuyNowSignStorage().containsLocation(location)) {
            if (!player.hasPermission("buycraft.admin")) {
                player.sendMessage(Component.text("You don't have permission to break this sign.").color(TextColor.color(Color.RED)));
                return false;
            }
            if (plugin.getBuyNowSignStorage().removeSign(location)) {
                player.sendMessage(Component.text("Removed buy now sign!").color(TextColor.color(Color.RED)));
                return true;
            } else {
                player.sendMessage(Component.text("Unable to remove buy now sign!").color(TextColor.color(Color.RED)));
                return false;
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

    @Listener
    public void onRightClickBlock(InteractBlockEvent.Secondary event, @First ServerPlayer p) {
        BlockSnapshot b = event.block();
        if (!isSign(b.state().type()) || !b.location().isPresent()) {
            return;
        }

        SerializedBlockLocation sbl = SpongeSerializedBlockLocation.create(b.location().get());

        for (SavedBuyNowSign s : plugin.getBuyNowSignStorage().getSigns()) {
            if (s.getLocation().equals(sbl)) {
                // Signs are rate limited (per player) in order to limit API calls issued.
                Long ts = signCooldowns.get(p.uniqueId());
                long now = System.currentTimeMillis();
                if (ts == null || ts + COOLDOWN_MS <= now) {
                    signCooldowns.put(p.uniqueId(), now);
                    plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, s.getPackageId(), p));
                }
                return;
            }
        }
    }
}
