package uz.alex2276564.leverlock.utils.runner;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Ultimate runner implementation exposing FoliaLib capabilities.
 * <p>
 * Optimized for Paper 1.16.5+ and Folia.
 */
public final class FoliaRunner implements Runner {

    private static final long NEXT_TICK = 1L;

    @Getter
    public final FoliaLib foliaLib;
    @Getter
    public final PlatformScheduler scheduler;

    public FoliaRunner(@NotNull JavaPlugin plugin) {
        this.foliaLib = new FoliaLib(plugin);
        this.scheduler = foliaLib.getScheduler();
    }

// ========== BASIC GLOBAL SCHEDULING ==========

    @Override
    public @NotNull TaskHandle runGlobal(@NotNull Runnable task) {
        WrappedTask t = scheduler.runLater(task, NEXT_TICK);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runGlobalLater(@NotNull Runnable task, long delayTicks) {
        WrappedTask t = scheduler.runLater(task, clampToTick(delayTicks));
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runGlobalTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        WrappedTask t = scheduler.runTimer(task, clampToTick(delayTicks), clampToTick(periodTicks));
        return new FoliaTaskHandle(t);
    }

// ========== GLOBAL SCHEDULING WITH TIMEUNIT ==========

    @Override
    public @NotNull TaskHandle runGlobalLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runLater(task, delay, unit);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runGlobalTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runTimer(task, delay, period, unit);
        return new FoliaTaskHandle(t);
    }

// ========== ASYNC SCHEDULING ==========

    @Override
    public @NotNull TaskHandle runAsync(@NotNull Runnable task) {
        WrappedTask t = scheduler.runLaterAsync(task, 0L);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAsyncLater(@NotNull Runnable task, long delayTicks) {
        WrappedTask t = scheduler.runLaterAsync(task, Math.max(0L, delayTicks));
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        WrappedTask t = scheduler.runTimerAsync(task, Math.max(0L, delayTicks), clampToTick(periodTicks));
        return new FoliaTaskHandle(t);
    }

// ========== ASYNC SCHEDULING WITH TIMEUNIT ==========

    @Override
    public @NotNull TaskHandle runAsyncLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runLaterAsync(task, delay, unit);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAsyncTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runTimerAsync(task, delay, period, unit);
        return new FoliaTaskHandle(t);
    }

// ========== ENTITY SCHEDULING ==========

    @Override
    public @NotNull TaskHandle runAtEntity(@NotNull Entity entity, @NotNull Runnable task) {
        WrappedTask t = scheduler.runAtEntityLater(entity, task, NEXT_TICK);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, long delayTicks) {
        WrappedTask t = scheduler.runAtEntityLater(entity, task, clampToTick(delayTicks));
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, long delayTicks, long periodTicks) {
        WrappedTask t = scheduler.runAtEntityTimer(entity, task, clampToTick(delayTicks), clampToTick(periodTicks));
        return new FoliaTaskHandle(t);
    }

// ========== ENTITY SCHEDULING WITH FALLBACK ==========

    @Override
    public @NotNull TaskHandle runAtEntity(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback) {
        return runAtEntityLater(entity, task, fallback, NEXT_TICK);
    }

    @Override
    public @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback, long delayTicks) {
        WrappedTask t = scheduler.runAtEntityLater(entity, task, fallback, clampToTick(delayTicks));
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback, long delayTicks, long periodTicks) {
        WrappedTask t = scheduler.runAtEntityTimer(entity, task, fallback, clampToTick(delayTicks), clampToTick(periodTicks));
        return new FoliaTaskHandle(t);
    }

// ========== ENTITY SCHEDULING WITH TIMEUNIT ==========

    @Override
    public @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runAtEntityLater(entity, task, delay, unit);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runAtEntityTimer(entity, task, delay, period, unit);
        return new FoliaTaskHandle(t);
    }

// ========== LOCATION SCHEDULING ==========

    @Override
    public @NotNull TaskHandle runAtLocation(@NotNull Location location, @NotNull Runnable task) {
        WrappedTask t = scheduler.runAtLocationLater(location, task, NEXT_TICK);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtLocationLater(@NotNull Location location, @NotNull Runnable task, long delayTicks) {
        WrappedTask t = scheduler.runAtLocationLater(location, task, clampToTick(delayTicks));
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtLocationTimer(@NotNull Location location, @NotNull Runnable task, long delayTicks, long periodTicks) {
        WrappedTask t = scheduler.runAtLocationTimer(location, task, clampToTick(delayTicks), clampToTick(periodTicks));
        return new FoliaTaskHandle(t);
    }

// ========== LOCATION SCHEDULING WITH TIMEUNIT ==========

    @Override
    public @NotNull TaskHandle runAtLocationLater(@NotNull Location location, @NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runAtLocationLater(location, task, delay, unit);
        return new FoliaTaskHandle(t);
    }

    @Override
    public @NotNull TaskHandle runAtLocationTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit) {
        WrappedTask t = scheduler.runAtLocationTimer(location, task, delay, period, unit);
        return new FoliaTaskHandle(t);
    }

// ========== ADVANCED (COMPLETABLEFUTURE) ==========

    @Override
    public @NotNull CompletableFuture<Void> runGlobalFuture(@NotNull Runnable task) {
        return scheduler.runNextTick(wrapped -> task.run());
    }

    @Override
    public @NotNull CompletableFuture<Void> runGlobalLaterFuture(@NotNull Runnable task, long delayTicks) {
        return scheduler.runLater(wrapped -> task.run(), clampToTick(delayTicks));
    }

    @Override
    public @NotNull CompletableFuture<Void> runAsyncFuture(@NotNull Runnable task) {
        return scheduler.runAsync(wrapped -> task.run());
    }

    @Override
    public @NotNull CompletableFuture<Void> runAsyncLaterFuture(@NotNull Runnable task, long delayTicks) {
        return scheduler.runLaterAsync(wrapped -> task.run(), Math.max(0L, delayTicks));
    }

    @Override
    public @NotNull CompletableFuture<Void> runAtLocationFuture(@NotNull Location location, @NotNull Runnable task) {
        return scheduler.runAtLocation(location, wrapped -> task.run());
    }

    @Override
    public @NotNull CompletableFuture<Void> runAtLocationLaterFuture(@NotNull Location location, @NotNull Runnable task, long delayTicks) {
        return scheduler.runAtLocationLater(location, wrapped -> task.run(), clampToTick(delayTicks));
    }

// ========== TELEPORTATION ==========

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location) {
        return scheduler.teleportAsync(entity, location);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        return scheduler.teleportAsync(entity, location, cause);
    }

// ========== FOLIA-SPECIFIC REGION CHECKS ==========

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(location);
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadiusChunks) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(location, squareRadiusChunks);
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Block block) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(block);
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(world, chunkX, chunkZ);
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ, int squareRadiusChunks) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(world, chunkX, chunkZ, squareRadiusChunks);
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        return !isFolia() || scheduler.isOwnedByCurrentRegion(entity);
    }

    @Override
    public boolean isGlobalTickThread() {
        return !isFolia() || scheduler.isGlobalTickThread();
    }

// ========== TASK MANAGEMENT ==========

    @Override
    public void cancelAllTasks() {
        scheduler.cancelAllTasks();
    }

    @Override
    public @Nullable List<TaskHandle> getAllTasks() {
        try {
            List<WrappedTask> tasks = scheduler.getAllTasks();
            return tasks != null
                    ? tasks.stream().map(FoliaTaskHandle::new).collect(Collectors.toList())
                    : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public @Nullable List<TaskHandle> getAllServerTasks() {
        try {
            List<WrappedTask> tasks = scheduler.getAllServerTasks();
            return tasks != null
                    ? tasks.stream().map(FoliaTaskHandle::new).collect(Collectors.toList())
                    : null;
        } catch (Exception ignored) {
            return null;
        }
    }

// ========== PLAYER UTILITIES ==========

    @Override
    public @Nullable Player getPlayer(@NotNull String name) {
        return scheduler.getPlayer(name);
    }

    @Override
    public @Nullable Player getPlayerExact(@NotNull String name) {
        return scheduler.getPlayerExact(name);
    }

    @Override
    public @Nullable Player getPlayer(@NotNull UUID uuid) {
        return scheduler.getPlayer(uuid);
    }

// ========== PLATFORM DETECTION ==========

    @Override
    public boolean isFolia() {
        return foliaLib.isFolia();
    }

    @Override
    public boolean isPaper() {
        return foliaLib.isPaper();
    }

    @Override
    public @NotNull String getPlatformName() {
        if (isFolia()) return "Folia";
        if (isPaper()) return "Paper";
        return "Unknown";
    }

    @Override
    public boolean supportsAsyncTeleport() {
// FoliaLib handles fallback internally (Paper old versions may fall back to sync)
        return true;
    }

    @Override
    public boolean supportsRegionScheduling() {
        return isFolia();
    }

// ========== INTERNAL UTILS ==========

    private static long clampToTick(long ticks) {
        return Math.max(NEXT_TICK, ticks);
    }

}