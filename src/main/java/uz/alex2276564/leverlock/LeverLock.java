package uz.alex2276564.leverlock;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.reloadcommand.ReloadCommand;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;
import uz.alex2276564.leverlock.utils.ConfigManager;
import uz.alex2276564.leverlock.utils.UpdateChecker;

public final class LeverLock extends JavaPlugin {
    @Getter
    private static LeverLock instance;

    @Override
    public void onEnable() {
        instance = this;
        registerListeners();
        registerCommands();
        loadUtils();
        checkUpdates();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLeverClickListener(this), this);
    }

    private void registerCommands() {
        getCommand("leverlock").setExecutor(new ReloadCommand());
    }

    private void loadUtils() {
        ConfigManager.reload();
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/LeverLock");
        updateChecker.checkForUpdates();
    }

}