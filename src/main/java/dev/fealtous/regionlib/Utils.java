package dev.fealtous.regionlib;

import dev.fealtous.regionlib.exceptions.InexpressibleException;
import net.minecraft.core.BlockPos;

import static dev.fealtous.regionlib.Utils.WorldSizeOffsets.DEFAULT_OFFSET;

public class Utils {
    public record WorldSizeOffsets(long XZ, long Y){
        static final WorldSizeOffsets DEFAULT_OFFSET = new WorldSizeOffsets(30_000_000, 2048);
    }
    private static final int UNASSIGNED = -1;
    private static int xInvert = UNASSIGNED;
    private static int yInvert = UNASSIGNED;
    private static int zInvert = UNASSIGNED;
    /**
     * Assumes provided spec is at least 256x32x256 at 1:1
     * Larger specs will work, but smaller ones will need their own math.
     */
    public static long defaultID(BlockPos pos, RegionSpec spec) {
        if (xInvert == UNASSIGNED || yInvert == UNASSIGNED || zInvert == UNASSIGNED) invert(spec);
        if (xInvert == 0 || yInvert == 0)
            throw new InexpressibleException("Region file coverage of 1 block is not supported by the default uuid system. Also, why tf are you doing that in the first place.");
        long x = (pos.getX() + DEFAULT_OFFSET.XZ) >> (xInvert);
        long y = (pos.getY() + DEFAULT_OFFSET.Y) >> (yInvert);
        long z = (pos.getZ()+ DEFAULT_OFFSET.XZ) >> (zInvert);
        return (x << 37) | (y << 27) | (z);
    }

    // FastMod is 2^(n+m) - 1, so we can recover the n+m value easily.
    private static void invert(RegionSpec spec) {
        xInvert = (int) Math.round(Math.log(spec.xFa() + 1) / Math.log(2));
        yInvert = (int) Math.round(Math.log(spec.yFa() + 1) / Math.log(2));
        zInvert = (int) Math.round(Math.log(spec.zFa() + 1) / Math.log(2));
    }
    /*
    Strategy:
    All regions have a unique identifier
    Minecraft's world may only reach +-30 million x/z and has a little under 4096 possible y positions
    NOTE:
    These values are vanilla, if a mod expands these, this cache policy may or may not be able to handle it.
    Which is why I'm letting this be configurable by other mods >.>

    So we want to encode these positions in as small of a space as possible and provide a way to map any position to its region identifier.
    Each region expresses:
     Y_TRACK_SIZE * Y_SPEC_LEVEL Y Coordinates
     X_TRACK_SIZE * X_SPEC_LEVEL X Coordinates
     Z_TRACK_SIZE * Z_SPEC_LEVEL Z Coordinates

    Proof:
    Maximum expressible X/Z value is: 60,000,000 (less than, offset to non-neg)
    log(60M) / log(2) = 25.8 bits
    25 if we can't fit 26.

    Maximum expressible Y value is: 4,096 (less than, offset to non-neg)
    log(4096) / log(2) = 12 bits
    25 + 12 + 25 = 62, 2 spare bits.
    We have 2 spare bits with 25, so we CAN fit 26 into both Z and X

    We will pack the bits as such:
    26 X bits
    12 Y bits
    26 Z bits
    26 + 12 + 26 = 64, so we've packed efficiently!

    (X + 30,000,000) / (X_TRACK_SIZE * X_SPEC_LEVEL) -> X identifier
    (Y + 2048) / (Y_TRACK_SIZE * Y_SPEC_LEVEL) -> Y identifier
    (Z + 30,000,000) / (Z_TRACK_SIZE * Z_SPEC_LEVEL) -> Z identifier

    Can be simplified to (K + OFFSET) >> (trackingPower + specPower) iff using power of 2 for track/spec
     */
}
