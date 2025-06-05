package uz.alex2276564.leverlock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.commands.reloadcommand.ReloadCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommandExecutor implements TabExecutor {
    private final LeverLock plugin;
    private final ReloadCommand reloadCommand;

    public MainCommandExecutor(LeverLock plugin) {
        this.plugin = plugin;
        this.reloadCommand = new ReloadCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6=== LeverLock Help ===");
            sender.sendMessage("§e/leverlock reload §7- Reload the plugin configuration");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Handle reload commands
        if (subCommand.equals("reload")) {
            return reloadCommand.onCommand(sender, command, label, args);
        }

        // Unknown command
        sender.sendMessage("§cUnknown command. Use /leverlock for help.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");

            String partial = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                return reloadCommand.onTabComplete(sender, command, alias, shiftArgs(args));
            }
        }

        return new ArrayList<>();
    }

    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }

        String[] shiftedArgs = new String[args.length - 1];
        System.arraycopy(args, 1, shiftedArgs, 0, args.length - 1);
        return shiftedArgs;
    }
}
