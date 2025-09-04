package uz.alex2276564.leverlock;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.LeverLockCommands;
import uz.alex2276564.leverlock.commands.framework.builder.BuiltCommand;
import uz.alex2276564.leverlock.commands.framework.builder.MultiCommandManager;
import uz.alex2276564.leverlock.config.LeverLockConfigManager;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;
import uz.alex2276564.leverlock.utils.adventure.AdventureMessageManager;
import uz.alex2276564.leverlock.utils.adventure.LegacyMessageManager;
import uz.alex2276564.leverlock.utils.adventure.MessageManager;
import uz.alex2276564.leverlock.utils.backup.BackupManager;
import uz.alex2276564.leverlock.utils.runner.FoliaRunner;
import uz.alex2276564.leverlock.utils.runner.Runner;
import uz.alex2276564.leverlock.utils.UpdateChecker;

public final class LeverLock extends JavaPlugin {
    @Getter
    private static LeverLock instance;

    @Getter
    private Runner runner;

    @Getter
    private LeverLockConfigManager configManager;

    @Getter
    private BackupManager backupManager;

    @Getter
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            setupRunner();
            setupMessageManager();
            setupConfig();
            setupBackupManager();
            registerListeners();
            registerCommands();
            checkUpdates();

            getLogger().info("LeverLock has been enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable LeverLock: " + e.getMessage());
            e.printStackTrace();
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

    private void setupMessageManager() {
        if (isMiniMessageAvailable()) {
            try {
                messageManager = new AdventureMessageManager();
                getLogger().info("Using Adventure MiniMessage for text formatting - full MiniMessage syntax supported");
                return;
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Adventure MiniMessage: " + e.getMessage());
                getLogger().warning("Falling back to Legacy formatting...");
            }
        }

        messageManager = new LegacyMessageManager();
        getLogger().info("Using Legacy ChatColor formatting with MiniMessage syntax compatibility");
        getLogger().info("You can continue using MiniMessage syntax in your config - basic tags will be converted automatically");
        getLogger().info("Supported: colors, bold, italic, underlined, strikethrough, obfuscated, reset");
        getLogger().warning("Note: Legacy mode uses regex processing which may have slight performance overhead");
        getLogger().info("Note: Complex features (gradients, hover, click events) are not available on older server versions");
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
        configManager = new LeverLockConfigManager(this);
        configManager.reload();
    }

    private void setupBackupManager() {
        backupManager = new BackupManager(this);

        // Check for backup need on startup
        backupManager.checkAndBackupAsync();

        // Schedule periodic checks - daily (24 hours)
        long dailyTicks = Runner.secondsToTicks(24 * 60 * 60);
        runner.runAsyncTimer(() -> backupManager.checkAndBackupAsync(), dailyTicks, dailyTicks);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLeverClickListener(this), this);
    }

    private void registerCommands() {
        MultiCommandManager multiManager = new MultiCommandManager(this);

        BuiltCommand permGuardCommand = LeverLockCommands.createLeverLockCommand();
        multiManager.registerCommand(permGuardCommand);
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/LeverLock", runner);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (runner != null) {
            runner.cancelAllTasks();
        }
    }
}
