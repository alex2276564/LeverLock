package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.LeverLock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
            CommandPath path = findCommandPath(command, args);
            executeCommandPath(sender, path, args);
        } catch (Exception e) {
            String tmpl = "<red>Error executing command:</red> <gray><err></gray>";
            LeverLock.getInstance().getMessageManager().sendMessage(sender, tmpl, "err", (e.getMessage() != null ? e.getMessage() : "unknown"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (command == null) return new ArrayList<>();

        if (args.length == 0) return new ArrayList<>();

        CommandPath path = findCommandPath(command, Arrays.copyOf(args, args.length - 1));
        String partial = args[args.length - 1];

        List<String> completions = new ArrayList<>();

        // Suggest subcommands first
        for (Map.Entry<String, BuiltSubCommand> entry : path.subCommands.entrySet()) {
            String subName = entry.getKey();
            BuiltSubCommand subCmd = entry.getValue();

            if (subName.toLowerCase().startsWith(partial.toLowerCase())
                    && (subCmd.permission() == null || sender.hasPermission(subCmd.permission()))) {
                completions.add(subName);
            }
        }

        // If no subcommand suggestion, suggest arguments
        if (path.arguments != null && !path.arguments.isEmpty()) {
            int argIndex = args.length - 1 - path.consumedArgs.size();
            if (argIndex >= 0 && argIndex < path.arguments.size()) {
                ArgumentBuilder<?> arg = path.arguments.get(argIndex);
                addArgumentCompletions(completions, arg, partial, sender, args, path);
            }
        }

        return completions;
    }

    private CommandPath findCommandPath(BuiltCommand rootCommand, String[] args) {
        List<String> consumedArgs = new ArrayList<>();
        Map<String, BuiltSubCommand> currentSubCommands = rootCommand.subCommands();
        List<ArgumentBuilder<?>> currentArguments = rootCommand.arguments();
        BiConsumer<CommandSender, CommandContext> currentExecutor = rootCommand.executor();
        String currentPermission = rootCommand.permission();

        // Walk through args to find the deepest matching subcommand
        for (String arg : args) {
            String lowerArg = arg.toLowerCase();
            if (currentSubCommands.containsKey(lowerArg)) {
                BuiltSubCommand subCmd = currentSubCommands.get(lowerArg);
                consumedArgs.add(arg);
                currentSubCommands = subCmd.subCommands();
                currentArguments = subCmd.arguments();
                currentExecutor = subCmd.executor();
                currentPermission = subCmd.permission();
            } else {
                // No more subcommands match, remaining args are arguments
                break;
            }
        }

        return new CommandPath(consumedArgs, currentSubCommands, currentArguments, currentExecutor, currentPermission);
    }

    private void executeCommandPath(CommandSender sender, CommandPath path, String[] args) {
        // Check permission
        if (path.permission != null && !sender.hasPermission(path.permission)) {
            String tmpl = "<red>You don't have permission:</red> <yellow><perm>";
            LeverLock.getInstance().getMessageManager().sendMessage(sender, tmpl, "perm", path.permission);
            return;
        }

        // If we have an executor, run it
        if (path.executor != null) {
            String[] argumentArgs = Arrays.copyOfRange(args, path.consumedArgs.size(), args.length);
            CommandContext context = parseArguments(path.arguments, argumentArgs);
            path.executor.accept(sender, context);
            return;
        }

        // Try to find help subcommand
        if (path.subCommands.containsKey("help")) {
            BuiltSubCommand helpCmd = path.subCommands.get("help");
            if ((helpCmd.permission() == null || sender.hasPermission(helpCmd.permission()))
                    && helpCmd.executor() != null) {
                CommandContext context = parseArguments(helpCmd.arguments(), new String[0]);
                helpCmd.executor().accept(sender, context);
                return;
            }

        }

        // Show default help
        showHelp(sender, path);
    }

    private CommandContext parseArguments(List<ArgumentBuilder<?>> argumentBuilders, String[] args) {
        CommandContext context = new CommandContext(args);

        for (int i = 0; i < argumentBuilders.size(); i++) {
            ArgumentBuilder<?> argBuilder = argumentBuilders.get(i);

            if (i >= args.length) {
                if (argBuilder.isOptional()) {
                    context.setArgument(argBuilder.getName(), argBuilder.getDefaultValue());
                } else {
                    throw new RuntimeException("Missing required argument: " + argBuilder.getName());
                }
            } else {
                try {
                    Object parsed = argBuilder.getType().parse(args[i]);
                    context.setArgument(argBuilder.getName(), parsed);
                } catch (ArgumentType.ArgumentParseException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        return context;
    }

    private void addArgumentCompletions(List<String> completions, ArgumentBuilder<?> arg, String partial, CommandSender sender, String[] fullArgs, CommandPath path) {
        if (arg.getSuggestions() != null) {
            for (String suggestion : arg.getSuggestions()) {
                if (suggestion != null && suggestion.toLowerCase().startsWith(partial.toLowerCase())) {
                    completions.add(suggestion);
                }
            }
        }

        String[] argsSoFar = Arrays.copyOfRange(fullArgs, path.consumedArgs.size(), fullArgs.length - 1);

        try {
            if (arg.getDynamicSuggestions() != null) {
                var dynamic = arg.getDynamicSuggestions().suggest(sender, partial, argsSoFar);
                if (dynamic != null) {
                    for (String suggestion : dynamic) {
                        if (suggestion != null && suggestion.toLowerCase().startsWith(partial.toLowerCase())) {
                            completions.add(suggestion);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore suggestion errors
        }
    }

    private void showHelp(CommandSender sender, CommandPath path) {
        LeverLock.getInstance().getMessageManager().sendMessage(sender,
                "<gold>=== " + command.name().toUpperCase() + " Help ===");

        for (Map.Entry<String, BuiltSubCommand> entry : path.subCommands.entrySet()) {
            BuiltSubCommand sub = entry.getValue();
            if (sub.permission() == null || sender.hasPermission(sub.permission())) {
                String desc = sub.description() != null ? sub.description() : "No description";
                StringBuilder fullCommand = new StringBuilder("/" + command.name());
                for (String consumed : path.consumedArgs) {
                    fullCommand.append(" ").append(consumed);
                }
                fullCommand.append(" ").append(entry.getKey());

                LeverLock.getInstance().getMessageManager().sendMessage(sender,
                        "<yellow>" + fullCommand + " <gray>- " + desc);
            }
        }
    }

    // Helper class to store command path information
    private record CommandPath(List<String> consumedArgs, Map<String, BuiltSubCommand> subCommands,
                               List<ArgumentBuilder<?>> arguments, BiConsumer<CommandSender, CommandContext> executor,
                               String permission) {
    }
}