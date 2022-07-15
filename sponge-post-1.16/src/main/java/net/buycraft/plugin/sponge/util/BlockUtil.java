package net.buycraft.plugin.sponge.util;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.Arrays;

public class BlockUtil {
    public static boolean isSign(BlockType blockType) {
        return Arrays.asList(
                BlockTypes.ACACIA_WALL_SIGN.get(), BlockTypes.BIRCH_WALL_SIGN.get(), BlockTypes.DARK_OAK_WALL_SIGN.get(), BlockTypes.JUNGLE_WALL_SIGN.get(), BlockTypes.OAK_WALL_SIGN.get(), BlockTypes.SPRUCE_WALL_SIGN.get(),
                BlockTypes.ACACIA_SIGN.get(), BlockTypes.BIRCH_SIGN.get(), BlockTypes.DARK_OAK_SIGN.get(), BlockTypes.JUNGLE_SIGN.get(), BlockTypes.OAK_SIGN.get(), BlockTypes.SPRUCE_SIGN.get()
        ).contains(blockType);
    }
}
