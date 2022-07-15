package net.buycraft.plugin.sponge.tasks;

import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.shared.config.signs.storage.SavedBuyNowSign;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static net.buycraft.plugin.sponge.util.BlockUtil.isSign;

public class BuyNowSignUpdater implements Runnable {
    private final BuycraftPlugin plugin;

    public BuyNowSignUpdater(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (SavedBuyNowSign sign : plugin.getBuyNowSignStorage().getSigns()) {
            Package p = plugin.getListingUpdateTask().getPackageById(sign.getPackageId());
            if (p == null) {
                plugin.getLogger().error(String.format("Sign at %d, %d, %d in world %s does not have a valid package assigned to it.",
                        sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld()));
                continue;
            }
            Location<ServerWorld, ServerLocation> location = SpongeSerializedBlockLocation.toSponge(sign.getLocation());
            BlockState b = location.block();

            if (!isSign(b.type())) {
                plugin.getLogger().error(String.format("Sign at %d, %d, %d in world %s is not a sign in the world!", sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld()));
                continue;
            }

            Optional<? extends BlockEntity> entity = location.blockEntity();
            Currency currency = Currency.getInstance(plugin.getServerInformation().getAccount().getCurrency().getIso4217());
            List<String> signLines = plugin.getBuyNowSignLayout().format(currency, p);
            if (entity.isPresent()) {
                ListValue.Mutable<Component> signText = ((Sign) entity.get()).lines();
                for (int i = 0; i < 4; i++) {
                    if (i == 0) {
                        signText.set(i, Component.empty());
                    } else {
                        signText.set(i, Component.text(signLines.get(i).replace("&", "§")));
                    }
                }

                entity.get().offer(signText);
            }
        }
    }
}
