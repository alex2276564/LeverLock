package uz.alex2276564.leverlock.utils;

import lombok.experimental.UtilityClass;

/**
 * String utility class for processing escape sequences manually.
 *
 * This class is necessary because we moved away from default Bukkit configuration methods.
 * Bukkit's YamlConfiguration automatically processes escape sequences when loading strings
 * from YAML files, but since we're using custom configuration handling, we lost that "magic"
 * and now need to process escape sequences manually.
 *
 * Without this processing, strings like "Line 1\nLine 2" would be displayed literally
 * as "Line 1\nLine 2" instead of being split into two lines.
 */
@UtilityClass
public class StringUtils {

    /**
     * Process escape sequences in string manually.
     *
     * This replaces the functionality that Bukkit's YamlConfiguration
     * normally provides automatically when loading from config files.
     *
     * Supported sequences:
     * \n -> newline
     * \t -> tab
     * \r -> carriage return
     * \" -> double quote
     * \' -> single quote
     * \\ -> backslash
     *
     * @param input the string to process
     * @return processed string with escape sequences converted, or null if input is null
     */
    public static String processEscapeSequences(String input) {
        if (input == null) {
            return null;
        }

        return input.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }
}