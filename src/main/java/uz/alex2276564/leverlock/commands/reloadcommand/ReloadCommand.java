package uz.alex2276564.leverlock.commands.reloadcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uz.alex2276564.leverlock.commands.LongCommandExecutor;
import uz.alex2276564.leverlock.commands.reloadcommand.list.ReloadCommandExecutor;

import java.util.List;

public class ReloadCommand extends LongCommandExecutor {

    public ReloadCommand() {
        addSubCommand(new ReloadCommandExecutor(), new String[] {"reload"}, new Permission("leverlock.reload"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) return false;
        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);
        if (wrapper == null) return false;

        if (!commandSender.hasPermission(wrapper.getPermission())) {
            return false;
        }


        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        wrapper.getCommand().onExecute(commandSender, newArgs);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return getFirstAliases();
        }
        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);
        if (wrapper == null) return null;

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return wrapper.getCommand().onTabComplete(commandSender, newArgs);
    }
}