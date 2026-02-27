package dev.fealtous.regionlib;

import dev.fealtous.regionlib.exceptions.InexpressibleException;
import net.minecraft.core.BlockPos;

import static dev.fealtous.regionlib.Utils.WorldSizeOffsets.DEFAULT_OFFSET;

public interface Locator {
    Locator defaultLocator = new Locator() {
        private static final int UNASSIGNED = -1;
        private static int xInvert = UNASSIGNED;
        private static int yInvert = UNASSIGNED;
        private static int zInvert = UNASSIGNED;

        @Override
        public long locate(BlockPos pos, RegionSpec spec) {
            if (xInvert == UNASSIGNED || yInvert == UNASSIGNED || zInvert == UNASSIGNED) {
                xInvert = (int) Math.round(Math.log(spec.xFa() + 1) / Math.log(2));
                yInvert = (int) Math.round(Math.log(spec.yFa() + 1) / Math.log(2));
                zInvert = (int) Math.round(Math.log(spec.zFa() + 1) / Math.log(2));
            };
            if (xInvert == 0 || yInvert == 0)
                throw new InexpressibleException("Region file coverage of 1 block is not supported by the default uuid system. Also, why tf are you doing that in the first place.");
            long x = (pos.getX() + DEFAULT_OFFSET.XZ()) >> (xInvert);
            long y = (pos.getY() + DEFAULT_OFFSET.Y()) >> (yInvert);
            long z = (pos.getZ()+ DEFAULT_OFFSET.XZ()) >> (zInvert);
            return (x << 37) | (y << 27) | (z);
        }
    };

    long locate(BlockPos pos, RegionSpec spec);
}
