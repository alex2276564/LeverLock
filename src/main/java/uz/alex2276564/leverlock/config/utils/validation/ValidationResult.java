package uz.alex2276564.leverlock.config.utils.validation;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public void addError(String path, String message) {
        errors.add(path + ": " + message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void throwIfInvalid(String configName) {
        if (hasErrors()) {
            throw new ValidationException(configName + " validation failed:\n- " + String.join("\n- ", errors));
        }
    }
}