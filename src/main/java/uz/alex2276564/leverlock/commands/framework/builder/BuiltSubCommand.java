package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiConsumer;

public record BuiltSubCommand(String name, String permission, String description,
                              BiConsumer<CommandSender, CommandContext> executor, List<ArgumentBuilder<?>> arguments) {
}