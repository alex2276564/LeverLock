package uz.alex2276564.leverlock.commands;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.TabExecutor;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class LongCommandExecutor implements TabExecutor {

    @Getter(AccessLevel.PROTECTED)
    private final List<SubCommandWrapper> subCommands = new ArrayList<>();

    protected void addSubCommand(SubCommand command, String[] aliases, Permission permission) {
        this.subCommands.add(new SubCommandWrapper(command, aliases, permission));
    }

    @Nullable
    protected SubCommandWrapper getWrapperFromLabel(String label) {
        for (SubCommandWrapper wrapper: subCommands) {
            for (String alias: wrapper.aliases) {
                if(alias.equalsIgnoreCase(label)) {
                    return wrapper;
                }
            }
        }
        return null;
    }

    protected List<String> getFirstAliases() {
        final List<String> result = new ArrayList<>();
        for (final SubCommandWrapper wrapper: subCommands) {
            String alias = wrapper.aliases[0];
            result.add(alias);
        }
        return result;
    }

    @AllArgsConstructor
    @Getter
    public static class SubCommandWrapper {
        private final SubCommand command;
        private final String[] aliases;
        private final Permission permission;

    }
}
