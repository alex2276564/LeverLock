package uz.alex2276564.leverlock.commands.framework.builder;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {
    private final Map<String, Object> arguments = new HashMap<>();
    @Getter
    private final String[] rawArgs;

    public CommandContext(String[] rawArgs) {
        this.rawArgs = rawArgs;
    }

    public <T> void setArgument(String name, T value) {
        arguments.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }
}
