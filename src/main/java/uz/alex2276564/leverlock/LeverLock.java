package uz.alex2276564.leverlock;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.reloadcommand.ReloadCommand;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;
import uz.alex2276564.leverlock.task.BukkitRunner;
import uz.alex2276564.leverlock.task.Runner;
import uz.alex2276564.leverlock.utils.ConfigManager;
import uz.alex2276564.leverlock.utils.UpdateChecker;

public final class LeverLock extends JavaPlugin {
    @Getter
    private static LeverLock instance;

    @Getter
    private Runner runner;

    @Override
    public void onEnable() {
        instance = this;
        setupRunner();
        registerListeners();
        registerCommands();
        loadUtils();
        checkUpdates();
    }

    private void setupRunner() {
        runner = new BukkitRunner(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLeverClickListener(runner), this);
    }

    private void registerCommands() {
        getCommand("leverlock").setExecutor(new ReloadCommand());
    }

    private void loadUtils() {
        ConfigManager.reload();
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/LeverLock", runner);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onDisable() {
        runner.cancelTasks();
    }

}