package uz.alex2276564.leverlock.commands.framework.builder;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SubCommandBuilder {
    @Getter
    private final String name;
    private final CommandBuilder parent;
    private final SubCommandBuilder parentSub;
    @Getter
    private String permission;
    @Getter
    private String description;
    @Getter
    private BiConsumer<CommandSender, CommandContext> executor;
    @Getter
    private final List<ArgumentBuilder<?>> arguments = new ArrayList<>();
    @Getter
    private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();

    // Constructor for top-level subcommands
    public SubCommandBuilder(String name, CommandBuilder parent) {
        this.name = name;
        this.parent = parent;
        this.parentSub = null;
    }

    // Constructor for nested subcommands
    public SubCommandBuilder(String name, SubCommandBuilder parentSub) {
        this.name = name;
        this.parent = null;
        this.parentSub = parentSub;
    }

    public SubCommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SubCommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public SubCommandBuilder executor(BiConsumer<CommandSender, CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public <T> SubCommandBuilder argument(ArgumentBuilder<T> argument) {
        this.arguments.add(argument);
        return this;
    }

    // Add nested subcommand support
    public SubCommandBuilder subcommand(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subcommand name cannot be null or empty");
        }
        String lowerName = name.toLowerCase();
        if (subCommands.containsKey(lowerName)) {
            throw new IllegalArgumentException("Subcommand '" + name + "' already exists");
        }

        SubCommandBuilder sub = new SubCommandBuilder(name, this);
        subCommands.put(lowerName, sub);
        return sub;
    }

    public SubCommandBuilder and() {
        if (parent != null) {
            return this; // Can't use 'and' with CommandBuilder, return self
        }
        return parentSub;
    }

    public CommandBuilder done() {
        if (parent != null) {
            return parent;
        }
        // Navigate up to find the root CommandBuilder
        SubCommandBuilder current = this;
        while (current.parentSub != null) {
            current = current.parentSub;
        }
        if (current.parent != null) {
            return current.parent;
        }
        throw new IllegalStateException("No parent CommandBuilder found");
    }

    public BuiltCommand build() {
        if (parent != null) {
            return parent.build();
        }
        return done().build();
    }

    // Internal method to build this subcommand and all its nested subcommands
    public BuiltSubCommand buildSubCommand() {
        Map<String, BuiltSubCommand> builtSubCommands = new HashMap<>();
        for (Map.Entry<String, SubCommandBuilder> entry : subCommands.entrySet()) {
            SubCommandBuilder sub = entry.getValue();
            builtSubCommands.put(entry.getKey(), sub.buildSubCommand());
        }

        return new BuiltSubCommand(name, permission, description, executor, builtSubCommands, arguments);
    }

}