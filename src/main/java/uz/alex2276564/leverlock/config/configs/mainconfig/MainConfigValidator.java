package uz.alex2276564.leverlock.config.configs.mainconfig;

import lombok.experimental.UtilityClass;
import uz.alex2276564.leverlock.config.utils.validation.ValidationResult;
import uz.alex2276564.leverlock.config.utils.validation.Validators;

@UtilityClass
public class MainConfigValidator {

    public static void validate(MainConfig config) {
        ValidationResult result = new ValidationResult();

        validateCooldownSection(result, config.cooldown);
        validateCleanupSection(result, config.cleanup);

        result.throwIfInvalid("Main configuration");
    }

    private static void validateCooldownSection(ValidationResult result, MainConfig.CooldownSection cooldown) {
        Validators.min(result, "cooldown.duration", cooldown.duration, 1, "Cooldown duration must be at least 1 second");
        Validators.max(result, "cooldown.duration", cooldown.duration, 3600, "Cooldown duration cannot exceed 1 hour (3600 seconds)");
    }

    private static void validateCleanupSection(ValidationResult result, MainConfig.CleanupSection cleanup) {
        Validators.min(result, "cleanup.interval", cleanup.interval, 60, "Cleanup interval must be at least 60 seconds");
        Validators.max(result, "cleanup.interval", cleanup.interval, 86400, "Cleanup interval cannot exceed 24 hours (86400 seconds)");
    }
}