package uz.alex2276564.leverlock.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Centralized security utility for sanitizing different types of input data
 */
@UtilityClass
public class SecurityUtils {


    // ANSI escape sequences - major attack vector!
    private static final Pattern ANSI_ESCAPE = Pattern.compile("\u001B\\[[0-9;]*[ -/]*[@-~]");

    // Pre-compiled patterns for performance
    private static final Pattern VERSION_SAFE = Pattern.compile("[^a-zA-Z0-9._+~-]");
    private static final Pattern COUNTRY_SAFE = Pattern.compile("[^\\p{L}0-9\\s'()-]");
    private static final Pattern COMMAND_DANGEROUS = Pattern.compile("[;&|`$(){}\"'\\\\<>\\r\\n§\u001B]");
    private static final Pattern IP_SAFE = Pattern.compile("[^0-9a-fA-F:.]");
    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");

    /**
     * Types of data that need different sanitization approaches
     */
    public enum SanitizeType {
        VERSION,        // GitHub tags, version strings
        COUNTRY,        // Geolocation API responses
        PLAYER_NAME,    // Minecraft usernames for commands
        IP_ADDRESS,     // IP addresses for logging
        LOG_MESSAGE,    // General log output
        ERROR_MESSAGE,  // Exception messages
        FILE_NAME,      // File names/paths
        API_RESPONSE    // Generic API responses
    }

    /**
     * Universal sanitizer method - handles different data types appropriately
     */
    public static String sanitize(@Nullable String input, SanitizeType type) {
        return sanitize(input, type, getDefaultMaxLength(type));
    }

    /**
     * Universal sanitizer with custom max length
     */
    public static String sanitize(@Nullable String input, SanitizeType type, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            return getDefaultValue(type);
        }

        String result = input.trim();

        // Step 1: Remove ANSI escape sequences (critical for all types!)
        result = ANSI_ESCAPE.matcher(result).replaceAll("");

        // Step 2: Type-specific sanitization
        switch (type) {
            case VERSION -> result = sanitizeVersion(result);
            case COUNTRY -> result = sanitizeCountry(result);
            case PLAYER_NAME -> result = sanitizePlayerName(result);
            case IP_ADDRESS -> result = sanitizeIpAddress(result);
            case LOG_MESSAGE -> result = sanitizeLogMessage(result);
            case ERROR_MESSAGE -> result = sanitizeErrorMessage(result);
            case FILE_NAME -> result = sanitizeFileName(result);
            case API_RESPONSE -> result = sanitizeApiResponse(result);
        }

        // Step 3: Apply length limit
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength) + "...";
        }

        // Step 4: Ensure we don't return empty string
        return result.isEmpty() ? getDefaultValue(type) : result;
    }

    private static String sanitizeVersion(String input) {
        // Only allow version-safe characters
        return VERSION_SAFE.matcher(input).replaceAll("");
    }

    private static String sanitizeCountry(String input) {
        // Allow country name characters
        return COUNTRY_SAFE.matcher(input).replaceAll("");
    }

    private static String sanitizePlayerName(String input) {
        // Remove command injection chars + control chars
        String result = COMMAND_DANGEROUS.matcher(input).replaceAll("");
        return CONTROL_CHARS.matcher(result).replaceAll("");
    }

    private static String sanitizeIpAddress(String input) {
        // Only allow IP address characters
        return IP_SAFE.matcher(input).replaceAll("");
    }

    private static String sanitizeLogMessage(String input) {
        // Replace control chars with spaces, clean up multiple spaces
        String result = CONTROL_CHARS.matcher(input).replaceAll(" ");
        return result.replaceAll("\\s+", " ");
    }

    private static String sanitizeErrorMessage(String input) {
        // Remove sensitive information patterns
        String result = input.replaceAll("([A-Za-z]:\\\\|/)\\S*", "[PATH]");
        result = result.replaceAll("(?i)(password|token|key|secret)=\\S*", "$1=[HIDDEN]");
        return sanitizeLogMessage(result);
    }

    private static String sanitizeFileName(String input) {
        // Replace filesystem dangerous chars
        String result = input.replaceAll("[\\\\/:*?\"<>|]", "_");
        return CONTROL_CHARS.matcher(result).replaceAll("_");
    }

    private static String sanitizeApiResponse(String input) {
        // Keep basic formatting but remove dangerous control chars
        return CONTROL_CHARS.matcher(input).replaceAll(" ");
    }

    private static String getDefaultValue(SanitizeType type) {
        return switch (type) {
            case VERSION -> "unknown";
            case COUNTRY -> "Unknown";
            case PLAYER_NAME -> "unknown";
            case IP_ADDRESS -> "unknown";
            case LOG_MESSAGE, ERROR_MESSAGE -> "";
            case FILE_NAME -> "file";
            case API_RESPONSE -> "";
        };
    }

    private static int getDefaultMaxLength(SanitizeType type) {
        return switch (type) {
            case VERSION -> 20;
            case COUNTRY -> 50;
            case PLAYER_NAME -> 64;
            case IP_ADDRESS -> 45;
            case LOG_MESSAGE -> 200;
            case ERROR_MESSAGE -> 300;
            case FILE_NAME -> 100;
            case API_RESPONSE -> 500;
        };
    }

    /**
     * Check if input contains suspicious patterns that might indicate attack attempts
     */
    public static boolean containsSuspiciousPatterns(String input) {
        if (input == null) return false;

        String lower = input.toLowerCase();

        // ANSI escape sequences
        if (ANSI_ESCAPE.matcher(input).find()) return true;

        // Common injection patterns
        String[] suspiciousPatterns = {
                "javascript:", "data:", "vbscript:", "file:",
                "<script", "</script", "eval(", "exec(",
                "system(", "cmd.exe", "powershell", "/bin/",
                "chmod", "wget ", "curl ", "nc -",
                "bash -", "sh -", "python -c", "${jndi:",
                "\\x", "\\u", "\u001b"
        };

        for (String pattern : suspiciousPatterns) {
            if (lower.contains(pattern)) return true;
        }

        return false;
    }

    /**
     * Convenient method for logging with automatic suspicious pattern detection
     */
    public static String safeLog(String input) {
        if (containsSuspiciousPatterns(input)) {
            return "[SUSPICIOUS_CONTENT_BLOCKED]";
        }
        return sanitize(input, SanitizeType.LOG_MESSAGE);
    }
}