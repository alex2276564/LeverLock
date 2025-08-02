package uz.alex2276564.leverlock.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.config.configs.mainconfig.MainConfig;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.leverlock.config.configs.mainconfig.MainConfigValidator;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfigValidator;

import java.io.File;

public class LeverLockConfigManager {
    private final LeverLock plugin;

    @Getter
    private MainConfig mainConfig;

    @Getter
    private MessagesConfig messagesConfig;

    public LeverLockConfigManager(LeverLock plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        try {
            loadMainConfig();
            loadMessagesConfig();

            plugin.getLogger().info("Configuration system reloaded successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMainConfig() {
        mainConfig = ConfigManager.create(MainConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(plugin.getDataFolder(), "config.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MainConfigValidator.validate(mainConfig);
        plugin.getLogger().info("Main configuration loaded and validated successfully");
    }

    private void loadMessagesConfig() {
        messagesConfig = ConfigManager.create(MessagesConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(plugin.getDataFolder(), "messages.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MessagesConfigValidator.validate(messagesConfig);
        plugin.getLogger().info("Messages configuration loaded and validated successfully");
    }
}