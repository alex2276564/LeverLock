package uz.alex2276564.leverlock;

import uz.alex2276564.leverlock.config.LeverLockConfigManager;
import uz.alex2276564.leverlock.utils.adventure.MessageManager;
import uz.alex2276564.leverlock.utils.runner.Runner;

import java.util.logging.Logger;

/**
 * Lightweight service container intended for command handlers only.
 * <p>
 * This record groups together the core services that are commonly needed
 * by multiple commands, so we don't have to pass 4-5 constructor parameters
 * every time.
 * <p>
 * IMPORTANT:
 * - Use this ONLY for commands and command-related classes.
 * - For listeners, utilities and other components prefer explicit
 * dependencies in constructors (no "god" service containers).
 */
public record LeverLockServices(
        Runner runner,
        LeverLockConfigManager configManager,
        MessageManager messageManager,
        Logger logger
) {
}