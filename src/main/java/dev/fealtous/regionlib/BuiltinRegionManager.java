package dev.fealtous.regionlib;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Default impl, should work for most use cases. Discriminator is based on dimension.
 */
public class BuiltinRegionManager extends AbstractRegionManager<ResourceKey<Level>> {
    private final Map<ResourceKey<Level>, Long2ObjectMap<Region>> regionCache;
    private final Path rootDir;
    private final String defaultDirName = "regionlib";
    public BuiltinRegionManager(MinecraftServer server, RegionSpec spec) {
        super(spec);
        regionCache = new HashMap<>();
        try {
            rootDir = super.rootSaveDirectory(server);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(ResourceKey<Level> dim, Region region, long id) {
        regionCache.get(dim).put(id, region);
    }

    @Nullable
    public Region get(ServerLevel level, BlockPos pos) {
        return get(level, pos, false);
    }

    @Nullable
    public Region get(@NonNull ServerLevel level, BlockPos pos, boolean abandonIfNotPresent) {
        var map = regionCache.get(level.dimension());
        long id = Locator.defaultLocator.locate(pos, spec);
        Region res = null;
        if (map != null) { // Does there exist a key for this dimension?
            res = map.get(id);
            if (res != null) return res;
        } else { // If not, we sure as hell know there aren't any entries in memory
            regionCache.put(level.dimension(), new Long2ObjectOpenHashMap<>());
        }
        if (!abandonIfNotPresent) { // So then it's safe to try to load and put, since we JUST made a key.
            res = load(id, level.dimension());
            if (res != null) put(level.dimension(), res, id);
            else {
                res = new Region(spec, spec.makeByteArray());
                put(level.dimension(), res, id);
            }
        }
        return res;
    }

    public Region remove(ResourceKey<Level> dim, BlockPos pos) {
        return remove(dim, pos, true);
    }

    public Region remove(ResourceKey<Level> dim, BlockPos pos, boolean andSave) {
        long id = Locator.defaultLocator.locate(pos, spec);
        var map = regionCache.get(dim);
        if (map == null) return null;
        Region res = map.remove(id);
        if (res == null) return null;
        if (andSave) save(id, res, dim);
        return res;
    }

    public void mark(ServerLevel level, BlockPos pos) {
        Region region = get(level, pos);
        if (region != null) region.mark(pos);
    }

    public boolean isMarked(ServerLevel level, BlockPos pos) {
        Region region = get(level, pos);
        return region != null && region.isMarked(pos);
    }

    public void saveAllIn(ResourceKey<Level> discriminator) {
        var dims = regionCache.get(discriminator);
        for (Long2ObjectMap.Entry<Region> regionEntry : dims.long2ObjectEntrySet()) {
            save(regionEntry.getLongKey(), regionEntry.getValue(), discriminator);
        }
    }

    public void saveAll () {
        for (ResourceKey<Level> levelResourceKey : regionCache.keySet()) {
            saveAllIn(levelResourceKey);
        }
    }

    public Region load(long id, ResourceKey<Level> discriminator) {
        Path loc = rootDir.resolve(defaultDirName).resolve(folder(discriminator)).resolve(id + extn);
        if (Files.exists(loc)) {
            try {
                byte[] bytes = Files.readAllBytes(loc); // Be aware this will pause the main thread, so keeping files small is ideal.
                return new Region(spec, bytes);
            } catch (IOException e) {
                LogUtils.getLogger().error("Unable to read region with id: {} in dimension: {}. Null will be returned.", id, discriminator.identifier().getPath());
            }
        }
        return null;
    }

    public void save(long id, Region region, ResourceKey<Level> discriminator) {
        Path loc = rootDir.resolve(defaultDirName).resolve(folder(discriminator));
        try {
            Path dirs = Files.createDirectories(loc).resolve(id + extn);;
            try {
                var opt = StandardOpenOption.WRITE;
                if (!dirs.toFile().exists()) opt = StandardOpenOption.CREATE;
                Files.write(dirs, region.data(), opt);
            } catch (IOException e) {
                LogUtils.getLogger().error("Failed to save region with id: {}", id);
                e.printStackTrace();
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Failed to create directory path for default regionlib save location. Here's why:");
            LogUtils.getLogger().error(e.getLocalizedMessage());
        }
    }

    private String folder(ResourceKey<Level> disc) {
        return disc.identifier().getPath();
    }
}
