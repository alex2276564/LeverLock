package uz.alex2276564.leverlock.commands.framework.builder;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.List;

@Getter
public class ArgumentBuilder<T> {
    public interface SuggestionProvider {
        List<String> suggest(CommandSender sender, String partial, String[] argsSoFar);
    }

    private final String name;
    private final ArgumentType<T> type;
    private boolean optional = false;
    private T defaultValue;
    private List<String> suggestions;

    private SuggestionProvider dynamicSuggestions;

    public ArgumentBuilder(String name, ArgumentType<T> type) {
        this.name = name;
        this.type = type;
    }

    public ArgumentBuilder<T> optional(T defaultValue) {
        this.optional = true;
        this.defaultValue = defaultValue;
        return this;
    }

    public ArgumentBuilder<T> suggestions(String... suggestions) {
        this.suggestions = List.of(suggestions);
        return this;
    }

    public ArgumentBuilder<T> suggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public ArgumentBuilder<T> dynamicSuggestions(SuggestionProvider provider) {
        this.dynamicSuggestions = provider;
        return this;
    }

}