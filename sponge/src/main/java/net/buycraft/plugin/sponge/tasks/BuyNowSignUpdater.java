package net.buycraft.plugin.sponge.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.shared.config.signs.storage.SavedBuyNowSign;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;

import java.util.Optional;
import java.util.Currency;
import java.util.List;

@RequiredArgsConstructor
public class BuyNowSignUpdater implements Runnable {
    private final BuycraftPlugin plugin;

    @Override
    public void run() {
        for (SavedBuyNowSign sign : plugin.getBuyNowSignStorage().getSigns()) {
            Package p = plugin.getListingUpdateTask().getPackageById(sign.getPackageId());
            if (p == null) {
                plugin.getLogger().error(String.format("Sign at %d, %d, %d in world %s does not have a valid package assigned to it.",
                        sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld()));
                continue;
            }

            Location location = SpongeSerializedBlockLocation.toSponge(sign.getLocation());

            BlockState b = location.getBlock();

            if (!(b.getType().equals(BlockTypes.WALL_SIGN) || b.getType().equals(BlockTypes.STANDING_SIGN))) {
                plugin.getLogger().error(String.format("Sign at %d, %d, %d in world %s is not a sign in the world!",
                        sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld()));
                continue;
            }

            Optional<TileEntity> entity = location.getTileEntity();

            Currency currency = Currency.getInstance(plugin.getServerInformation().getAccount().getCurrency().getIso4217());

            List<String> signLines = plugin.getBuyNowSignLayout().format(currency, p);

            if (entity.isPresent() && entity.get().supports(SignData.class)) {
                SignData signData = entity.get().getOrCreate(SignData.class).get();
                ListValue<Text> lines = signData.lines();

                for (int i = 0; i < 4; i++) {
                    if (i >= signLines.size()) {
                        lines.set(i, Text.EMPTY);
                    } else {
                        lines.set(i, Text.builder(signLines.get(i).replace("&", "§")).build());
                    }

                }

                entity.get().offer(lines);
            }

        }
    }
}
