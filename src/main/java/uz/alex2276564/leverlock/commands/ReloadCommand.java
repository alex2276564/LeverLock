package uz.alex2276564.leverlock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.LeverLock;

public class ReloadCommand implements CommandExecutor {
    private final LeverLock plugin;

    public ReloadCommand(LeverLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String permission = "leverlock.reload";

        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command. Missing permission: §e" + permission);
            return true;
        }

        plugin.reloadConfig();
        plugin.reloadListenerConfig();
        sender.sendMessage("§aLeverLock configuration successfully reloaded.");
        return true;
    }
}