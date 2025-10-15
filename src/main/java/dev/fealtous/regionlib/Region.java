package dev.fealtous.regionlib;

import net.minecraft.core.BlockPos;

/**
 * The OOPS! all bitwise operations class. Byte array is accessible for bulk operations if wanted.
 */
public record Region(RegionSpec spec, byte[] data) {
    public void mark(BlockPos pos) {
        data[_byte(pos)] |= index(pos);
    }

    public void unmark(BlockPos pos) {
        data[_byte(pos)] &= (byte) ~index(pos);
    }

    public void flip(BlockPos pos) {
        data[_byte(pos)] ^= index(pos);
    }

    public boolean isMarked(BlockPos pos) {
        return (data[_byte(pos)] & index(pos)) != 0;
    }

    private byte index(BlockPos pos) {
        return (byte) (0x1 << (pos.getX() & 7));
    }

    private int _byte(BlockPos pos) {
        int x = (pos.getX() >> spec.xSp()) & spec.xFa();
        int y = (pos.getY() >> spec.ySp()) & spec.yFa();
        int z = (pos.getZ() >> spec.zSp()) & spec.zFa();
        return (x >> 3) + (y * spec.yByteOffset()) + (z * spec.zByteOffset());
    }
}
