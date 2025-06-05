package uz.alex2276564.leverlock.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import uz.alex2276564.leverlock.LeverLock;

import java.time.Duration;

public class ConfigManager {
    private final LeverLock plugin;
    private FileConfiguration config;

    @Getter
    private Duration cooldownDuration;

    @Getter
    private long cleanupInterval;

    @Getter
    private String cooldownMessage;

    public ConfigManager(LeverLock plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }

    private void loadConfig() {
        cooldownDuration = Duration.ofSeconds(config.getLong("cooldown.duration", 1));
        cleanupInterval = config.getLong("cleanup.interval", 300) * 20; // convert to ticks
        cooldownMessage = config.getString("message.cooldown", "§cYou are interacting with the lever too quickly!");

        plugin.getLogger().info("Loaded config: cooldown=" + cooldownDuration.getSeconds() + "s, cleanup=" + cleanupInterval/20 + "s");
    }
}
