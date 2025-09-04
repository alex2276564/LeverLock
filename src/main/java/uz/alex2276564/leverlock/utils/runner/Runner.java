package uz.alex2276564.leverlock.utils.runner;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive cross-platform scheduler for Paper and Folia.
 * <p>
 * Exposes FoliaLib capabilities through a unified interface.
 */
public interface Runner {

// ========== BASIC GLOBAL SCHEDULING ==========

    @NotNull TaskHandle runGlobal(@NotNull Runnable task);

    @NotNull TaskHandle runGlobalLater(@NotNull Runnable task, long delayTicks);

    @NotNull TaskHandle runGlobalTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

// ========== GLOBAL SCHEDULING WITH TIMEUNIT ==========

    @NotNull TaskHandle runGlobalLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    @NotNull TaskHandle runGlobalTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

// ========== ASYNC SCHEDULING ==========

    @NotNull TaskHandle runAsync(@NotNull Runnable task);

    @NotNull TaskHandle runAsyncLater(@NotNull Runnable task, long delayTicks);

    @NotNull TaskHandle runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

// ========== ASYNC SCHEDULING WITH TIMEUNIT ==========

    @NotNull TaskHandle runAsyncLater(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    @NotNull TaskHandle runAsyncTimer(@NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

// ========== ENTITY SCHEDULING ==========

    @NotNull TaskHandle runAtEntity(@NotNull Entity entity, @NotNull Runnable task);

    @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, long delayTicks);

    @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, long delayTicks, long periodTicks);

// ========== ENTITY SCHEDULING WITH FALLBACK ==========

    @NotNull TaskHandle runAtEntity(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback);

    @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback, long delayTicks);

    @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, @Nullable Runnable fallback, long delayTicks, long periodTicks);

// ========== ENTITY SCHEDULING WITH TIMEUNIT ==========

    @NotNull TaskHandle runAtEntityLater(@NotNull Entity entity, @NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    @NotNull TaskHandle runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

// ========== LOCATION SCHEDULING ==========

    @NotNull TaskHandle runAtLocation(@NotNull Location location, @NotNull Runnable task);

    @NotNull TaskHandle runAtLocationLater(@NotNull Location location, @NotNull Runnable task, long delayTicks);

    @NotNull TaskHandle runAtLocationTimer(@NotNull Location location, @NotNull Runnable task, long delayTicks, long periodTicks);

// ========== LOCATION SCHEDULING WITH TIMEUNIT ==========

    @NotNull TaskHandle runAtLocationLater(@NotNull Location location, @NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    @NotNull TaskHandle runAtLocationTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit unit);

// ========== ADVANCED SCHEDULING WITH COMPLETABLEFUTURE ==========

    @NotNull CompletableFuture<Void> runGlobalFuture(@NotNull Runnable task);

    @NotNull CompletableFuture<Void> runGlobalLaterFuture(@NotNull Runnable task, long delayTicks);

    @NotNull CompletableFuture<Void> runAsyncFuture(@NotNull Runnable task);

    @NotNull CompletableFuture<Void> runAsyncLaterFuture(@NotNull Runnable task, long delayTicks);

    @NotNull CompletableFuture<Void> runAtLocationFuture(@NotNull Location location, @NotNull Runnable task);

    @NotNull CompletableFuture<Void> runAtLocationLaterFuture(@NotNull Location location, @NotNull Runnable task, long delayTicks);

// ========== TELEPORTATION ==========

    @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location);

    @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause);

// ========== FOLIA-SPECIFIC REGION CHECKS ==========

    boolean isOwnedByCurrentRegion(@NotNull Location location);

    boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadiusChunks);

    boolean isOwnedByCurrentRegion(@NotNull Block block);

    boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ);

    boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ, int squareRadiusChunks);

    boolean isOwnedByCurrentRegion(@NotNull Entity entity);

    boolean isGlobalTickThread();

// ========== TASK MANAGEMENT ==========

    void cancelAllTasks();

    @Nullable List<TaskHandle> getAllTasks();

    @Nullable List<TaskHandle> getAllServerTasks();

// ========== PLAYER UTILITIES ==========

    @Nullable Player getPlayer(@NotNull String name);

    @Nullable Player getPlayerExact(@NotNull String name);

    @Nullable Player getPlayer(@NotNull UUID uuid);

// ========== PLATFORM DETECTION ==========

    boolean isFolia();

    boolean isPaper();

    @NotNull String getPlatformName();

    boolean supportsAsyncTeleport();

    boolean supportsRegionScheduling();

// ========== UTILITY METHODS ==========

    static long msToTicks(long milliseconds) {
        return (milliseconds + 49L) / 50L;
    }

    static long secondsToTicks(long seconds) {
        return seconds * 20L;
    }

    static long toTicks(long duration, @NotNull TimeUnit unit) {
        return msToTicks(unit.toMillis(duration));
    }

    static long ticksToMs(long ticks) {
        return ticks * 50L;
    }

    static double ticksToSeconds(long ticks) {
        return ticks / 20.0;
    }

}