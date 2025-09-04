package uz.alex2276564.leverlock.utils.runner;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Comprehensive TaskHandle implementation with all capabilities.
 */
public record FoliaTaskHandle(@NotNull WrappedTask wrappedTask) implements TaskHandle {

    @Override
    public boolean cancel() {
        if (!wrappedTask.isCancelled()) {
            wrappedTask.cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return wrappedTask.isCancelled();
    }

    @Override
    public boolean isRunning() {
        return !wrappedTask.isCancelled();
    }

    @Override
    public Plugin getOwningPlugin() {
        return wrappedTask.getOwningPlugin();
    }

    @Override
    public boolean isAsync() {
        return wrappedTask.isAsync();
    }

    @Override
    public Object getPlatformTask() {
        return wrappedTask;
    }

}