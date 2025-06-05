package uz.alex2276564.leverlock;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.MainCommandExecutor;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;
import uz.alex2276564.leverlock.runner.BukkitRunner;
import uz.alex2276564.leverlock.runner.Runner;
import uz.alex2276564.leverlock.config.ConfigManager;
import uz.alex2276564.leverlock.utils.UpdateChecker;

public final class LeverLock extends JavaPlugin {
    @Getter
    private static LeverLock instance;

    @Getter
    private Runner runner;

    @Getter
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        setupRunner();

        configManager = new ConfigManager(this);
        configManager.reload();

        registerListeners();
        registerCommands();
        checkUpdates();

        getLogger().info("LeverLock has been enabled!");
    }

    private void setupRunner() {
        runner = new BukkitRunner(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLeverClickListener(this), this);
    }

    private void registerCommands() {
        // Register main command executor that handles all subcommands
        getCommand("leverlock").setExecutor(new MainCommandExecutor(this));
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/LeverLock", runner);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (runner != null) {
            runner.cancelTasks();
        }
    }
}
