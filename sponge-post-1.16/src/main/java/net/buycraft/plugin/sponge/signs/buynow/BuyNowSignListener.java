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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class BuyNowSignListener {
    private static final long COOLDOWN_MS = 250; // 5 ticks
    private final BuycraftPlugin plugin;
    private final Map<UUID, Long> signCooldowns = new HashMap<>();

    public BuyNowSignListener(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onSignChange(ChangeSignEvent event) {
        boolean ourSign;

        try {
            ourSign = Arrays.asList("[buycraft_buy]", "[tebex_buy]").contains(PlainTextComponentSerializer.plainText().serialize(event.originalText().get(0)).toLowerCase());
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
            event.cause().first(Player.class).get().sendMessage(Component.text("The second line must be a number.").color(TextColor.color(Color.RED)));
            return;
        }

        plugin.getBuyNowSignStorage().addSign(new SavedBuyNowSign(SpongeSerializedBlockLocation.create(event.sign().location()), pos));
        player.sendMessage(Component.text("Added new buy now sign!").color(TextColor.color(Color.GREEN)));

        // The below is due to the design of the Sponge Data API
        SignData signData = event.getText();
        ListValue<Text> lines = signData.lines();
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                lines.set(i, Component.text("Buy!").build());
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

    @Listener
    public void onRightClickBlock(InteractBlockEvent.Secondary event) {
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
