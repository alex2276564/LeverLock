package uz.alex2276564.leverlock.utils.runner;

import org.bukkit.plugin.Plugin;

/**
 * Comprehensive task handle with full platform capabilities.
 */
public interface TaskHandle {

    /**
     * Cancel this task.
     *
     * @return true if task was cancelled by this call, false if already cancelled/completed.
     */
    boolean cancel();

    /**
     * @return true if this task has been cancelled.
     */
    boolean isCancelled();

    /**
     * @return true if this task is not cancelled (scheduled/running/repeating).
     */
    boolean isRunning();

    /**
     * @return plugin that owns this task.
     */
    Plugin getOwningPlugin();

    /**
     * @return true if this task is asynchronous.
     */
    boolean isAsync();

    /**
     * @return underlying platform-specific task object (FoliaLib WrappedTask).
     */
    Object getPlatformTask();
}