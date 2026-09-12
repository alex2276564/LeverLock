package uz.alex2276564.leverlock;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.LeverLockCommands;
import uz.alex2276564.leverlock.commands.framework.builder.BuiltCommand;
import uz.alex2276564.leverlock.commands.framework.builder.MultiCommandManager;
import uz.alex2276564.leverlock.config.LeverLockConfigManager;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;
import uz.alex2276564.leverlock.utils.HttpUtils;
import uz.alex2276564.leverlock.utils.adventure.AdventureMessageManager;
import uz.alex2276564.leverlock.utils.adventure.LegacyMessageManager;
import uz.alex2276564.leverlock.utils.adventure.MessageManager;
import uz.alex2276564.leverlock.utils.backup.BackupManager;
import uz.alex2276564.leverlock.utils.runner.FoliaRunner;
import uz.alex2276564.leverlock.utils.runner.Runner;
import uz.alex2276564.leverlock.utils.UpdateChecker;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LeverLock extends JavaPlugin {
    @Getter
    private Runner runner;

    @Getter
    private HttpUtils httpUtils;

    @Getter
    private LeverLockConfigManager configManager;

    @Getter
    private BackupManager backupManager;

    @Getter
    private MessageManager messageManager;

    @Getter
    private UpdateChecker updateChecker;

    @Getter
    private LeverLockServices services;

    @Override
    public void onEnable() {
        try {
            setupRunner();
            setupHttpClient();
            setupMessageManager();
            setupConfig();
            setupBackupManager();
            setupServices();
            setupUpdateChecker();
            registerListeners();
            registerCommands();

            getLogger().info("LeverLock has been enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable LeverLock", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupRunner() {
        runner = new FoliaRunner(this);
        getLogger().info("Initialized " + runner.getPlatformName() + " scheduler support");

        if (runner.isFolia()) {
            getLogger().info("Folia detected - using RegionScheduler and EntityScheduler for optimal performance");
        }
    }

    private void setupHttpClient() {
        this.httpUtils = new HttpUtils();
    }

    private void setupMessageManager() {
        if (isMiniMessageAvailable()) {
            try {
                messageManager = new AdventureMessageManager(runner);
                getLogger().info("Using Adventure MiniMessage for text formatting - full MiniMessage syntax supported");
                return;
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Adventure MiniMessage: " + e.getMessage());
                getLogger().warning("Falling back to Legacy formatting...");
            }
        }

        messageManager = new LegacyMessageManager();
        getLogger().info("Using Legacy ChatColor formatting with MiniMessage-like basic tags");
        getLogger().info("Supported: colors, bold, italic, underlined, strikethrough, obfuscated, reset");
        getLogger().info("Note: Complex features (gradients, hover, click events) are not available on older versions");
    }

    private boolean isMiniMessageAvailable() {
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().info("MiniMessage library not found - this is normal for Paper versions below 1.18");
            return false;
        }
    }

    private void setupConfig() {
        File dataFolder = getDataFolder();
        Logger logger = getLogger();
        this.configManager = new LeverLockConfigManager(
                dataFolder,
                logger,
                messageManager
        );
        configManager.reload();
    }

    private void setupBackupManager() {
        backupManager = new BackupManager(runner, getLogger(), getDataFolder().toPath());

        // Check for backup need on startup
        backupManager.checkAndBackupAsync();

        // Schedule periodic checks - daily (24 hours)
        long dailySeconds = 24L * 60L * 60L;
        long dailyTicks = Runner.secondsToTicks(dailySeconds);
        runner.runAsyncTimer(() -> backupManager.checkAndBackupAsync(), dailyTicks, dailyTicks);
    }

    private void setupServices() {
        this.services = new LeverLockServices(
                runner,
                configManager,
                messageManager,
                getLogger()
        );
    }

    private void setupUpdateChecker() {
        this.updateChecker = new UpdateChecker(
                getDescription().getName(),
                getDescription().getVersion(),
                "alex2276564/LeverLock",
                runner,
                httpUtils,
                getLogger()
        );

        updateChecker.checkForUpdates();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new PlayerLeverClickListener(configManager, runner, messageManager),
                this
        );
    }

    private void registerCommands() {
        MultiCommandManager multiManager = new MultiCommandManager(this, services);

        BuiltCommand permGuardCommand = LeverLockCommands.createLeverLockCommand(services);
        multiManager.registerCommand(permGuardCommand);
    }

    @Override
    public void onDisable() {
        if (runner != null) {
            runner.cancelAllTasks();
        }
    }
}
