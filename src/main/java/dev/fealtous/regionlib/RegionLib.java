package dev.fealtous.regionlib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RegionLib.MODID)
public final class RegionLib {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "regionlib";

    public RegionLib(FMLJavaModLoadingContext context) {
        ForgeTestImpl.init();
    }
}
