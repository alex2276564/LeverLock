package uz.alex2276564.leverlock.commands.framework.builder;

import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public class ArgumentBuilder<T> {
    private final String name;
    private final ArgumentType<T> type;
    private boolean optional = false;
    private T defaultValue;
    private List<String> suggestions;
    private Function<String, List<String>> dynamicSuggestions;

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

    public ArgumentBuilder<T> dynamicSuggestions(Function<String, List<String>> suggestionProvider) {
        this.dynamicSuggestions = suggestionProvider;
        return this;
    }

}