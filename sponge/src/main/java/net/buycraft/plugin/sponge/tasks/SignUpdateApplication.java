package net.buycraft.plugin.sponge.tasks;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.NumberFormat;
import java.util.*;

@RequiredArgsConstructor
public class SignUpdateApplication implements Runnable {
    public static final List<Direction> SKULL_CHECK = ImmutableList.of(
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE);
    private final BuycraftPlugin plugin;
    private final Map<RecentPurchaseSignPosition, RecentPayment> paymentMap;
    private final Map<String, GameProfile> resolvedProfiles;

    private Optional<Skull> findSkull(Location<World> start) {
        for (Direction direction : SKULL_CHECK) {
            Optional<TileEntity> entity = start.getRelative(direction).getTileEntity();
            if (entity.isPresent()) {
                if (entity.get().getType().equals(TileEntityTypes.SKULL)) {
                    return Optional.of((Skull) entity.get());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void run() {
        for (Map.Entry<RecentPurchaseSignPosition, RecentPayment> entry : paymentMap.entrySet()) {
            Location<World> location = SpongeSerializedBlockLocation.toSponge(entry.getKey().getLocation());

            Optional<TileEntity> entity = location.getTileEntity();
            if (entity.isPresent() && entity.get().supports(SignData.class)) {
                SignData signData = entity.get().getOrCreate(SignData.class).get();
                ListValue<Text> lines = signData.lines();

                if (entry.getValue() != null) {
                    lines.set(0, Text.EMPTY);
                    lines.set(1, Text.of(entry.getValue().getPlayer().getName()));

                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                    format.setCurrency(Currency.getInstance(entry.getValue().getCurrency().getIso4217()));

                    lines.set(2, Text.of(format.format(entry.getValue().getAmount())));
                    lines.set(3, Text.EMPTY);
                } else {
                    for (int i = 0; i < 4; i++) {
                        lines.set(i, Text.EMPTY);
                    }
                }

                entity.get().offer(lines);

                Location<World> supportedBy = location.getRelative(Direction.UP);

                Optional<Skull> skullOptional = findSkull(supportedBy);
                if (skullOptional.isPresent()) {
                    Skull skull = skullOptional.get();
                    if (!skull.supports(Keys.REPRESENTED_PLAYER)) {
                        skull.offer(Keys.SKULL_TYPE, SkullTypes.PLAYER);
                    }
                    GameProfile profile = entry.getValue() != null ?
                            resolvedProfiles.getOrDefault(entry.getValue().getPlayer().getName(), resolvedProfiles.get("MHF_Question")) :
                            resolvedProfiles.get("MHF_Question");
                    if (profile != null) {
                        skull.offer(Keys.REPRESENTED_PLAYER, profile);
                    }
                }
            } else {
                plugin.getLogger().error("Location " + entry.getKey() + " doesn't have a tile entity! (Sign missing?)");
            }
        }
    }
}
