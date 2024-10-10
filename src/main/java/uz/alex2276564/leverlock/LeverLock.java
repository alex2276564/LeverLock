package uz.alex2276564.leverlock;

import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.commands.ReloadCommand;
import uz.alex2276564.leverlock.listeners.PlayerLeverClickListener;

public final class LeverLock extends JavaPlugin {
    private PlayerLeverClickListener leverClickListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        leverClickListener = new PlayerLeverClickListener(this);
        getServer().getPluginManager().registerEvents(leverClickListener, this);
        getCommand("leverlockreload").setExecutor(new ReloadCommand(this));
    }

    public void reloadListenerConfig() {
        leverClickListener.reloadConfig();
    }
}