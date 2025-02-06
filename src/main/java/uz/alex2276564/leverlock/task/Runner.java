package uz.alex2276564.leverlock.task;

import org.jetbrains.annotations.NotNull;

public interface Runner {

    void run(@NotNull Runnable task);

    void runAsync(@NotNull Runnable task);

    void runDelayed(@NotNull Runnable task, long delayTicks);

    void runDelayedAsync(@NotNull Runnable task, long delayTicks);

    void runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks);

    void runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks);

    void cancelTasks();
}