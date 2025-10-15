package dev.fealtous.regionlib;

import static dev.fealtous.regionlib.Size.*;


/**
 * xSp, ySp, zSp are the SPEC level, the specificity
 * xTr, yTr, zTr are the TRACKING level, the # of units per region
 * Blocks = Specificity * Tracking (so 2:1 specificity doubles the # of blocks but keeps the file size constant)
 *
 * WARNING WARNING WARNING
 * xFa, yFa, zFa are fast modulo values and only work with the default implementation
 * IF AND ONLY IF they are 1 minus a power of two.
 */
public record RegionSpec(int yByteOffset, int zByteOffset, int totalByte2n,
                         int xSp, int ySp, int zSp,
                         int xFa, int yFa, int zFa){

    /**
     * 16x1024x16 regions, each one tracks an entire chunk. Great for claims! (sorta)
     */
    public static final RegionSpec ALIGN_TO_CHUNK = createSpec(SPEC_16, SPEC_1024, SPEC_16, SPEC_1, SPEC_1, SPEC_1);
    /**
     * 64x8x64 regions with 1:1 tracking. ~32kb regions.
     */
    public static final RegionSpec SMALL_DEFAULT = createSpec(SPEC_1, SPEC_64, SPEC_8, SPEC_64);
    /**
     * 256x32x256 regions with 1:1 tracking. ~262 kb regions.
     */
    public static final RegionSpec MEDIUM_DEFAULT = createSpec(SPEC_1, SPEC_256, SPEC_32, SPEC_256);
    /**
     * 1024x64x1024 regions with 1:1 tracking. ~838 kb regions.
     */
    public static final RegionSpec LARGE_DEFAULT = createSpec(SPEC_1, SPEC_1024, SPEC_64, SPEC_1024);


    public static RegionSpec createSpec(Size xSpecificity, Size ySpecificity, Size zSpecificity, Size xTracking, Size yTracking, Size zTracking) {
        int totalByte2n = (xTracking.ordinal() + yTracking.ordinal() + zTracking.ordinal()) - 3; // -3 for bits per byte
        int yByteOffset = 1 << (xTracking.ordinal() - 3);
        int zByteOffset = yByteOffset << yTracking.ordinal();
        return new RegionSpec(yByteOffset, zByteOffset, totalByte2n, xSpecificity.ordinal(), ySpecificity.ordinal(), zSpecificity.ordinal(),
        fastMod(xTracking, xSpecificity), fastMod(yTracking, ySpecificity), fastMod(zTracking, zSpecificity));
    }

    public static RegionSpec createSpec(Size allSpecificity, Size xTracking, Size yTracking, Size zTracking) {
        return createSpec(allSpecificity, allSpecificity, allSpecificity, xTracking, yTracking, zTracking);
    }

    private static int fastMod(Size tracking, Size spec) {
        return (1 << (tracking.ordinal() + spec.ordinal())) - 1;
    }

    public long getSizeInBytes() {
        return 1L << totalByte2n;
    }

    public byte[] makeByteArray() {
        return new byte[1 << totalByte2n];
    }
}