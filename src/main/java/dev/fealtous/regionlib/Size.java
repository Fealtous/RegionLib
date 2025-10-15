package dev.fealtous.regionlib;

import net.minecraftforge.common.IExtensibleEnum;

/**
 * Utility class for all tracking sizes and specificities
 * We're treating it like a C enum, using the ordinals as powers of 2
 */
public enum Size {
    SPEC_1 ,
    SPEC_2  ,
    SPEC_4 ,
    SPEC_8  ,
    SPEC_16 ,
    SPEC_32 ,
    SPEC_64 ,
    SPEC_128 ,
    SPEC_256 ,
    SPEC_512 ,
    SPEC_1024,
    SPEC_2048,
    SPEC_4096,
    SPEC_8192
}
