package uz.alex2276564.leverlock.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    void onExecute(CommandSender commandSender, String[] args);

    List<String> onTabComplete(CommandSender commandSender, String[] args);
}
