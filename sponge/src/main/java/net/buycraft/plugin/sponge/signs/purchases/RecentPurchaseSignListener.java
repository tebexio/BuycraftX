package net.buycraft.plugin.sponge.signs.purchases;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.tasks.SignUpdater;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


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
        if (!event.getCause().first(Player.class).isPresent()) {
            // This change was not caused by a player.
            return;
        }
        if (!event.getCause().first(Player.class).get().hasPermission("buycraft.admin")) {
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
            event.getCause().first(Player.class).get()
                    .sendMessage(Text.builder("You can't show negative or zero purchases!").color(TextColors.RED).build());
            return;
        }

        if (pos > 100) {
            event.getCause().first(Player.class).get()
                    .sendMessage(Text.builder("You can't show more than 100 recent purchases!").color(TextColors.RED).build());
            return;
        }

        plugin.getRecentPurchaseSignStorage().addSign(new RecentPurchaseSignPosition(event.getTargetTile().getLocation(), pos));
        event.getCause().first(Player.class).get().sendMessage(Text.builder("Added new recent purchase sign!").color(TextColors.GREEN).build());

        for (int i = 0; i < 4; i++) {
            event.getText().lines().set(i, Text.EMPTY);
        }

        plugin.getPlatform().executeAsync(new SignUpdater(plugin));
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        // This is what happens when you code without sleep.
        // TODO Someone should check if this actually works .-.
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.getOriginal().getLocation().isPresent()) {
                if (transaction.getOriginal().getLocation().get().getBlockType().equals(BlockTypes.WALL_SIGN) || transaction.getOriginal()
                        .getLocation().get().getBlockType().equals(BlockTypes.STANDING_SIGN)) {
                    if (plugin.getRecentPurchaseSignStorage().containsLocation(transaction.getOriginal().getLocation().get())) {
                        if (event.getCause().first(Player.class).isPresent()) {
                            Player player = event.getCause().first(Player.class).get();
                            if (!player.hasPermission("buycraft.admin")) {
                                player.sendMessage(Text.builder("You don't have permission to break this sign.").color(TextColors.RED).build());
                                event.setCancelled(true);
                                return;
                            }
                            if (plugin.getRecentPurchaseSignStorage().removeSign(transaction.getOriginal().getLocation().get())) {
                                player.sendMessage(Text.builder("Removed recent purchase sign!").color(TextColors.RED).build());
                                plugin.getPlatform().executeAsync(new SignUpdater(plugin));
                                return;
                            }
                        } else {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
