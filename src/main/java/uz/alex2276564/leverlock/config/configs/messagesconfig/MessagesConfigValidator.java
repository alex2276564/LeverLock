package uz.alex2276564.leverlock.config.configs.messagesconfig;

import lombok.experimental.UtilityClass;
import uz.alex2276564.leverlock.config.utils.validation.ValidationResult;
import uz.alex2276564.leverlock.config.utils.validation.Validators;

@UtilityClass
public class MessagesConfigValidator {

    public static void validate(MessagesConfig config) {
        ValidationResult result = new ValidationResult();

        validateCommandsSection(result, config.commands);
        validateGeneralSection(result, config.general);

        result.throwIfInvalid("Messages configuration");
    }

    private static void validateCommandsSection(ValidationResult result, MessagesConfig.CommandsSection commands) {
        // Help section validation
        Validators.notBlank(result, "commands.help.header", commands.help.header, "Help header cannot be empty");
        Validators.notBlank(result, "commands.help.reloadLine", commands.help.reloadLine, "Help reload line cannot be empty");
        Validators.notBlank(result, "commands.help.helpLine", commands.help.helpLine, "Help help line cannot be empty");

        // Reload section validation
        Validators.notBlank(result, "commands.reload.success", commands.reload.success, "Reload success message cannot be empty");
        Validators.notBlank(result, "commands.reload.error", commands.reload.error, "Reload error message cannot be empty");
    }

    private static void validateGeneralSection(ValidationResult result, MessagesConfig.GeneralSection general) {
        Validators.notBlank(result, "general.cooldown", general.cooldown, "Cooldown message cannot be empty");
    }
}