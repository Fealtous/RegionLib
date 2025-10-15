package dev.fealtous.regionlib;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;


public class ForgeTestImpl {
    static BuiltinRegionManager regionManager;
    public static void init() {
        ServerStartedEvent.BUS.addListener(ForgeTestImpl::onServerStart);
        BlockEvent.BreakEvent.BUS.addListener(ForgeTestImpl::onBlockBroken);
        BlockEvent.EntityPlaceEvent.BUS.addListener(ForgeTestImpl::onBlockPlace);
        RegisterCommandsEvent.BUS.addListener(event -> {
            event.getDispatcher().register(
                    Commands.literal("regionsaveall").executes((ctx) -> {
                regionManager.saveAll();
                return 1;
            }));
        });
    }

    public static void onServerStart(ServerStartedEvent event) {
        regionManager = new BuiltinRegionManager(event.getServer(), RegionSpec.MEDIUM_DEFAULT);
    }

    public static boolean onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (regionManager.isMarked(level, event.getPos())) {
                LogUtils.getLogger().info("Position {} was marked", event.getPos());
            } else {
                LogUtils.getLogger().info("Position {} was not marked", event.getPos());
                event.setResult(Result.DENY);
                return true;
            }
        }
        return false;
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (event.getEntity() instanceof Player) {
                regionManager.mark(level, event.getPos());
                LogUtils.getLogger().info("Marked position at {}", event.getPos());
            }
        }
    }
}
