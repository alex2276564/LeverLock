package uz.alex2276564.leverlock.commands.framework.builder;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SubCommandBuilder {
    @Getter
    private final String name;
    private final CommandBuilder parent;
    @Getter
    private String permission;
    @Getter
    private String description;
    @Getter
    private BiConsumer<CommandSender, CommandContext> executor;
    @Getter
    private final List<ArgumentBuilder<?>> arguments = new ArrayList<>();

    public SubCommandBuilder(String name, CommandBuilder parent) {
        this.name = name;
        this.parent = parent;
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

    public CommandBuilder and() {
        return parent;
    }

    public BuiltCommand build() {
        return parent.build();
    }
}