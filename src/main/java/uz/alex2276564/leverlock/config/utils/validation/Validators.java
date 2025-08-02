package uz.alex2276564.leverlock.config.utils.validation;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class Validators {

    public static void notBlank(ValidationResult result, String path, String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            result.addError(path, message);
        }
    }

    public static void pattern(ValidationResult result, String path, String value, String regex, String message) {
        if (value != null && !Pattern.matches(regex, value)) {
            result.addError(path, message);
        }
    }

    public static void min(ValidationResult result, String path, int value, int min, String message) {
        if (value < min) {
            result.addError(path, message + " (found: " + value + ")");
        }
    }

    public static void max(ValidationResult result, String path, int value, int max, String message) {
        if (value > max) {
            result.addError(path, message + " (found: " + value + ")");
        }
    }

    public static void min(ValidationResult result, String path, long value, long min, String message) {
        if (value < min) {
            result.addError(path, message + " (found: " + value + ")");
        }
    }

    public static void max(ValidationResult result, String path, long value, long max, String message) {
        if (value > max) {
            result.addError(path, message + " (found: " + value + ")");
        }
    }
}