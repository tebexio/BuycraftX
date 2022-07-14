package net.buycraft.plugin.sponge.tasks;

import com.google.common.collect.ImmutableList;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.shared.config.signs.storage.RecentPurchaseSignPosition;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.buycraft.plugin.sponge.util.SpongeSerializedBlockLocation;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityTypes;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.block.entity.Skull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerLocation;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SignUpdateApplication implements Runnable {
    public static final List<Direction> SKULL_CHECK = ImmutableList.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NONE);
    private final BuycraftPlugin plugin;
    private final Map<RecentPurchaseSignPosition, RecentPayment> paymentMap;
    private final Map<String, GameProfile> resolvedProfiles;

    public SignUpdateApplication(final BuycraftPlugin plugin, final Map<RecentPurchaseSignPosition, RecentPayment> paymentMap, final Map<String, GameProfile> resolvedProfiles) {
        this.plugin = plugin;
        this.paymentMap = paymentMap;
        this.resolvedProfiles = resolvedProfiles;
    }

    private boolean isSign(BlockType blockType) {
        return Arrays.asList(
                BlockTypes.ACACIA_WALL_SIGN, BlockTypes.BIRCH_WALL_SIGN, BlockTypes.DARK_OAK_WALL_SIGN, BlockTypes.JUNGLE_WALL_SIGN, BlockTypes.OAK_WALL_SIGN, BlockTypes.SPRUCE_WALL_SIGN,
                BlockTypes.ACACIA_SIGN, BlockTypes.BIRCH_SIGN, BlockTypes.DARK_OAK_SIGN, BlockTypes.JUNGLE_SIGN, BlockTypes.OAK_SIGN, BlockTypes.SPRUCE_SIGN
        ).contains(blockType);
    }

    private Optional<Skull> findSkull(ServerLocation start) {
        for (Direction direction : SKULL_CHECK) {
            Optional<? extends BlockEntity> entity = start.relativeTo(direction).blockEntity();
            if (entity.isPresent()) {
                if (entity.get().type() == BlockEntityTypes.SKULL.get()) {
                    return Optional.of((Skull) entity.get());
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void run() {
        for (Map.Entry<RecentPurchaseSignPosition, RecentPayment> entry : paymentMap.entrySet()) {
            ServerLocation location = SpongeSerializedBlockLocation.toSponge(entry.getKey().getLocation());
            Optional<? extends BlockEntity> entity = location.blockEntity();
            if (entity.isPresent()) {
                BlockEntity blockEntity = entity.get();
                if(blockEntity.type() != BlockEntityTypes.SIGN.get()) return;

                Sign sign = (Sign) blockEntity;

                ListValue.Mutable<Component> signText = sign.lines();
                if (entry.getValue() != null) {
                    signText.set(0, Component.empty());
                    signText.set(1, Component.text(entry.getValue().getPlayer().getName()));
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                    format.setCurrency(Currency.getInstance(entry.getValue().getCurrency().getIso4217()));
                    signText.set(2, Component.text(format.format(entry.getValue().getAmount())));
                    signText.set(3, Component.empty());
                } else {
                    for (int i = 0; i < 4; i++) {
                        signText.set(i, Component.empty());
                    }
                }

                sign.offer(signText);

                ServerLocation supportedBy = location.relativeTo(Direction.UP);

                Optional<Skull> skullOptional = findSkull(supportedBy);
                if (skullOptional.isPresent()) {
                    Skull skull = skullOptional.get();

                    GameProfile profile = entry.getValue() != null ? resolvedProfiles.getOrDefault(entry.getValue().getPlayer().getName(), resolvedProfiles.get("MHF_Question")) : resolvedProfiles.get("MHF_Question");
                    if (profile != null) {
                        if (skull.supports(Keys.GAME_PROFILE)) {

                        }
                    }
                }
            } else {
                plugin.getLogger().error("Location " + entry.getKey() + " doesn't have a tile entity! (Sign missing?)");
            }
        }
    }
}
