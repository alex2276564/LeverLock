package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.LeverLock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {
    private final JavaPlugin plugin;
    private BuiltCommand command;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static CommandBuilder create(String name) {
        return new CommandBuilder(name);
    }

    public void register(BuiltCommand command) {
        this.command = command;
        plugin.getCommand(command.name()).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            executeCommand(sender, command, args);
            return true;
        } catch (Exception e) {
            LeverLock.getInstance().getMessageManager().sendMessage(sender,
                    "<red>Error executing command: " + e.getMessage());
            return true;
        }
    }

    private void executeCommand(CommandSender sender, BuiltCommand cmd, String[] args) {
        // Check if there's a subcommand
        if (args.length > 0) {
            String subName = args[0].toLowerCase();
            if (cmd.subCommands().containsKey(subName)) {
                BuiltSubCommand subCmd = cmd.subCommands().get(subName);

                // Check permission for subcommand
                if (subCmd.permission() != null && !sender.hasPermission(subCmd.permission())) {
                    LeverLock.getInstance().getMessageManager().sendMessage(sender,
                            "<red>You don't have permission: <yellow>" + subCmd.permission());
                    return;
                }

                // Execute subcommand
                if (subCmd.executor() != null) {
                    CommandContext context = parseArguments(subCmd.arguments(), args, 1);
                    subCmd.executor().accept(sender, context);
                    return;
                }
            }
        }

        // Execute main command
        if (cmd.executor() != null) {
            if (cmd.permission() != null && !sender.hasPermission(cmd.permission())) {
                LeverLock.getInstance().getMessageManager().sendMessage(sender,
                        "<red>You don't have permission: <yellow>" + cmd.permission());
                return;
            }

            CommandContext context = parseArguments(cmd.arguments(), args, 0);
            cmd.executor().accept(sender, context);
            return;
        }

        // Show help if no executor found
        showHelp(sender, cmd);
    }

    private CommandContext parseArguments(List<ArgumentBuilder<?>> argumentBuilders, String[] args, int startIndex) {
        CommandContext context = new CommandContext(args);

        for (int i = 0; i < argumentBuilders.size(); i++) {
            ArgumentBuilder<?> argBuilder = argumentBuilders.get(i);
            int argPos = startIndex + i;

            if (argPos >= args.length) {
                if (argBuilder.isOptional()) {
                    context.setArgument(argBuilder.getName(), argBuilder.getDefaultValue());
                } else {
                    throw new RuntimeException("Missing required argument: " + argBuilder.getName());
                }
            } else {
                try {
                    Object parsed = argBuilder.getType().parse(args[argPos]);
                    context.setArgument(argBuilder.getName(), parsed);
                } catch (ArgumentType.ArgumentParseException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        return context;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (command == null) return new ArrayList<>();

        return getTabCompletions(sender, command, args);
    }

    private List<String> getTabCompletions(CommandSender sender, BuiltCommand cmd, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length > 1) {
            // Look for subcommand
            String subName = args[0].toLowerCase();
            if (cmd.subCommands().containsKey(subName)) {
                BuiltSubCommand subCmd = cmd.subCommands().get(subName);
                if (subCmd.permission() == null || sender.hasPermission(subCmd.permission())) {
                    return getSubCommandCompletions(sender, subCmd, args);
                }
            }
        }

        if (args.length == 1) {
            // Suggest subcommands
            for (Map.Entry<String, BuiltSubCommand> entry : cmd.subCommands().entrySet()) {
                String subName = entry.getKey();
                BuiltSubCommand subCmd = entry.getValue();

                if (subName.startsWith(args[0].toLowerCase()) &&
                        (subCmd.permission() == null || sender.hasPermission(subCmd.permission()))) {
                    completions.add(subName);
                }
            }

            // Suggest main command arguments
            if (!cmd.arguments().isEmpty()) {
                ArgumentBuilder<?> arg = cmd.arguments().get(0);
                addArgumentCompletions(completions, arg, args[0]);
            }
        }

        return completions;
    }

    private List<String> getSubCommandCompletions(CommandSender sender, BuiltSubCommand subCmd, String[] args) {
        List<String> completions = new ArrayList<>();

        // Calculate which argument we're completing for the subcommand
        int subArgIndex = args.length - 2; // -1 for current arg, -1 for subcommand name
        if (subArgIndex >= 0 && subArgIndex < subCmd.arguments().size()) {
            ArgumentBuilder<?> arg = subCmd.arguments().get(subArgIndex);
            addArgumentCompletions(completions, arg, args[args.length - 1]);
        }

        return completions;
    }

    private void addArgumentCompletions(List<String> completions, ArgumentBuilder<?> arg, String partial) {
        if (arg.getSuggestions() != null) {
            for (String suggestion : arg.getSuggestions()) {
                if (suggestion.toLowerCase().startsWith(partial.toLowerCase())) {
                    completions.add(suggestion);
                }
            }
        }

        if (arg.getDynamicSuggestions() != null) {
            List<String> dynamic = arg.getDynamicSuggestions().apply(partial);
            if (dynamic != null) {
                for (String suggestion : dynamic) {
                    if (suggestion.toLowerCase().startsWith(partial.toLowerCase())) {
                        completions.add(suggestion);
                    }
                }
            }
        }
    }

    private void showHelp(CommandSender sender, BuiltCommand command) {
        LeverLock.getInstance().getMessageManager().sendMessage(sender,
                "<gold>=== " + command.name().toUpperCase() + " Help ===");

        for (Map.Entry<String, BuiltSubCommand> entry : command.subCommands().entrySet()) {
            BuiltSubCommand sub = entry.getValue();
            if (sub.permission() == null || sender.hasPermission(sub.permission())) {
                String desc = sub.description() != null ? sub.description() : "No description";
                LeverLock.getInstance().getMessageManager().sendMessage(sender,
                        "<yellow>/" + command.name() + " " + entry.getKey() + " <gray>- " + desc);
            }
        }
    }
}