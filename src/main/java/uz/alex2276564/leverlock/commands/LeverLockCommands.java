package uz.alex2276564.leverlock.commands;

import uz.alex2276564.leverlock.LeverLockServices;
import uz.alex2276564.leverlock.commands.framework.builder.BuiltCommand;
import uz.alex2276564.leverlock.commands.framework.builder.CommandBuilder;
import uz.alex2276564.leverlock.commands.framework.builder.CommandManager;
import uz.alex2276564.leverlock.commands.subcommands.help.HelpSubCommand;
import uz.alex2276564.leverlock.commands.subcommands.reload.ReloadSubCommand;

public class LeverLockCommands {

    public static BuiltCommand createLeverLockCommand(LeverLockServices services) {
        CommandBuilder builder = CommandManager.create("leverlock")
                .permission("leverlock.command")
                .description("Main LeverLock command");

        // Register all subcommands
        new ReloadSubCommand(services).build(builder);
        new HelpSubCommand(services).build(builder);

        return builder.build();
    }
}