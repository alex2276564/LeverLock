package uz.alex2276564.leverlock.commands;

import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.commands.framework.builder.*;
import uz.alex2276564.leverlock.commands.subcommands.help.HelpSubCommand;
import uz.alex2276564.leverlock.commands.subcommands.reload.ReloadSubCommand;

public class LeverLockCommands {

    public static BuiltCommand createLeverLockCommand() {
        CommandBuilder builder = CommandManager.create("leverlock")
                .permission("leverlock.command")
                .description("Main LeverLock command")
                .executor((sender, context) -> {
                    // Show help by default
                    LeverLock.getInstance().getMessageManager().sendMessage(sender,
                            "<gold>=== LeverLock Help ===");
                    LeverLock.getInstance().getMessageManager().sendMessage(sender,
                            "<yellow>/leverlock reload <gray>- Reload the plugin configuration");
                    LeverLock.getInstance().getMessageManager().sendMessage(sender,
                            "<yellow>/leverlock help <gray>- Show this help message");
                });

        // Register all subcommands
        new ReloadSubCommand().build(builder);
        new HelpSubCommand().build(builder);

        return builder.build();
    }
}