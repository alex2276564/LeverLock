package uz.alex2276564.leverlock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.utils.ConfigManager;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String permission = "leverlock.reload";

        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command. Missing permission: §e" + permission);
            return true;
        }

        ConfigManager.reload();
        sender.sendMessage("§aLeverLock configuration successfully reloaded.");
        return true;
    }
}