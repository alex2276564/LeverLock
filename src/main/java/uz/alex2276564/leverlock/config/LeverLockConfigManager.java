package uz.alex2276564.leverlock.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import uz.alex2276564.leverlock.config.configs.mainconfig.MainConfig;
import uz.alex2276564.leverlock.config.configs.mainconfig.MainConfigValidator;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfigValidator;
import uz.alex2276564.leverlock.utils.adventure.MessageManager;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeverLockConfigManager {

    private final File dataFolder;
    private final Logger logger;
    private final MessageManager messageManager;

    @Getter
    private MainConfig mainConfig;

    @Getter
    private MessagesConfig messagesConfig;

    public LeverLockConfigManager(File dataFolder,
                                  Logger logger,
                                  MessageManager messageManager) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.messageManager = messageManager;
    }

    public void reload() {
        try {
            loadMainConfig();
            loadMessagesConfig();
            logger.info("Configuration system reloaded successfully!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    private void loadMainConfig() {
        mainConfig = ConfigManager.create(MainConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(dataFolder, "config.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MainConfigValidator.validate(mainConfig);
        logger.info("Main configuration loaded and validated successfully");
    }

    private void loadMessagesConfig() {
        messagesConfig = ConfigManager.create(MessagesConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(dataFolder, "messages.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MessagesConfigValidator.validate(messagesConfig);
        messageManager.configureDisabledKeysProvider(() -> getMessagesConfig().disabledKeys);
        logger.info("Messages configuration loaded and validated successfully");
    }
}