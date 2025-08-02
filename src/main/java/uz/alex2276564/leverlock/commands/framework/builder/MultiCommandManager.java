package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MultiCommandManager {
    private final JavaPlugin plugin;
    private final Map<String, CommandManager> managers = new HashMap<>();

    public MultiCommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(BuiltCommand command) {
        CommandManager manager = new CommandManager(plugin);
        manager.register(command);
        managers.put(command.name().toLowerCase(), manager);
    }

    public void registerCommands(BuiltCommand... commands) {
        for (BuiltCommand command : commands) {
            registerCommand(command);
        }
    }

    public CommandManager getManager(String commandName) {
        return managers.get(commandName.toLowerCase());
    }
}