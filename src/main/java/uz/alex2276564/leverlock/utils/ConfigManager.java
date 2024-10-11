package uz.alex2276564.leverlock.utils;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import uz.alex2276564.leverlock.LeverLock;

import java.time.Duration;

public class ConfigManager {
    private static FileConfiguration config;
    @Getter
    private static Duration cooldownDuration;
    @Getter
    private static long cleanupInterval;
    @Getter
    private static String cooldownMessage;

    public static void reload() {
        Plugin plugin = LeverLock.getInstance();

        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }

    public static void loadConfig() {
        cooldownDuration = Duration.ofSeconds(config.getLong("cooldown.duration", 1));
        cleanupInterval = config.getLong("cleanup.interval", 300) * 20; // convert to ticks
        cooldownMessage = config.getString("message.cooldown", "§cYou are interacting with the lever too quickly!");
    }

    private ConfigManager() {
        throw new IllegalStateException("Utility class");
    }
}
