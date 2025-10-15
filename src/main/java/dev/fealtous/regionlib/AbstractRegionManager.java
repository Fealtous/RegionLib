package dev.fealtous.regionlib;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public abstract class AbstractRegionManager<T> {
    protected final RegionSpec spec;
    protected final String extn;

    public AbstractRegionManager(RegionSpec spec, String fileExtension) {
        this.spec = spec;
        this.extn = fileExtension;
    }

    public AbstractRegionManager(RegionSpec spec) {
        this(spec, ".rglf");
    }

    /**
     * Puts a region into the cache
     */
    public abstract void put(T discriminator, Region region, long id);

    /**
     * Gets a region from the cache
     */
    @Nullable
    public abstract Region get(ServerLevel level, BlockPos pos);

    /**
     * Gets a region from the cache, but if it isn't present, whether to try to load from disk.
     */
    @Nullable
    public abstract Region get(ServerLevel level, BlockPos pos, boolean abandonIfNotPresent);
    /**
     * Removes a region from the cache
     */
    public abstract Region remove(T discriminator, BlockPos pos);
    /**
     * Removes a region from the cache, instructs whether this removal should save to disk on removal.
     */
    public abstract Region remove(T discriminator, BlockPos pos, boolean andSave);

    /**
     * I hope it's obvious what these methods do.
     */
    public abstract void mark(ServerLevel level, BlockPos pos);
    public abstract boolean isMarked(ServerLevel level, BlockPos pos);

    /**
     * Loads a region from disk. If you want to enforce a spec, you'll have to do it yourself.
     */
    public abstract Region load(long id, T discriminator);

    /**
     * Saves a region to disk. If you want to enforce a spec, you'll have to do it yourself.
     */
    public abstract void save(long id, Region region, T discriminator);

    /**
     * Override if you want some custom save location. By default it'll be in the save.
     * The default region loader will only call this method once to get the proper root.
     * It's up to the implementation to decide when this is called. I recommend when the server has finished starting.
     */
    public Path rootSaveDirectory(MinecraftServer server) throws Exception {
        try {
            // I don't feel like trying to figure out a non-reflective way anymore. We're just grabbing the Path object anyway.
            var field = server.getClass().getSuperclass().getDeclaredField("storageSource");
            field.setAccessible(true);
            var res = field.get(server);
            var field1 = res.getClass().getDeclaredField("levelDirectory");
            field1.setAccessible(true);
            return ((LevelStorageSource.LevelDirectory) field1.get(res)).path();
        } catch (Exception e) {
            /* FMLPaths might be a better solution but its fiiiiine. */
            throw new Exception("Tell Feal he's an idiot and needs to fix the default save directory detection.");
        }
    }
}
