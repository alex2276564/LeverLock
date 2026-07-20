package uz.alex2276564.leverlock.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Centralized input validation and sanitization utilities.
 * <p>
 * Design goals:
 * - Keep this class SELF-CONTAINED (no external dependencies).
 * - Handle only the narrow set of contexts actually used in this plugin:
 * * player names (for logs, commands, notifications)
 * * commands executed on behalf of the plugin
 * * IP addresses (for logs, lookups, notifications)
 * * file names (for log files, backups, etc.)
 * * version tags (GitHub releases, plugin versions)
 * * log and error messages
 * * country names from external APIs
 * * generic API responses when we want to log/pass them safely
 * <p>
 * It is NOT a full WAF or generic sanitizer for arbitrary applications.
 * It implements pragmatic, context-aware defenses for this plugin only.
 */
@UtilityClass
public class SecurityUtils {

    // =====================================================================
    // Low-level generic patterns
    // =====================================================================

    /**
     * ANSI escape sequences (colors, cursor movement, etc.).
     * These can make logs unreadable or confusing, so we strip them.
     */
    private static final Pattern ANSI_ESCAPE =
            Pattern.compile("\u001B\\[[0-9;]*[ -/]*[@-~]");

    /**
     * Control characters (0x00–0x1F, 0x7F, etc.).
     * We remove them from logs / human-readable strings.
     */
    private static final Pattern CONTROL_CHARS =
            Pattern.compile("\\p{Cntrl}");

    /**
     * Runs of whitespace (spaces, tabs, newlines).
     * Used to normalize strings to a single space between tokens.
     */
    private static final Pattern WHITESPACE_RUN =
            Pattern.compile("\\s+");

    // =====================================================================
    // Whitelists (full-string validation)
    // =====================================================================

    /**
     * Version / tag whitelist.
     * We allow a conservative set: digits, letters, dot, underscore,
     * plus, minus, tilde. Length is capped to 40.
     * <p>
     * Examples that should pass:
     * - "1.0.5"
     * - "v1.0.5"
     * - "1.0.5-R0.1-SNAPSHOT"
     */
    private static final Pattern VERSION_SAFE =
            Pattern.compile("^[0-9A-Za-z._+\\-~]{1,40}$");

    /**
     * IPv4 strict validation.
     */
    private static final Pattern IPV4_SAFE = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}" +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    /**
     * IPv6 strict validation (full and compressed forms).
     */
    private static final Pattern IPV6_SAFE = Pattern.compile(
            "^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}$|" +
                    "^(([\\da-fA-F]{1,4}:){0,6}[\\da-fA-F]{1,4})?::" +
                    "(([\\da-fA-F]{1,4}:){0,6}[\\da-fA-F]{1,4})?$"
    );

    // =====================================================================
    // Blacklists (strip/replace certain characters)
    // =====================================================================

    /**
     * Characters dangerous in the context of commands / shells.
     * We remove these before dispatching commands built from templates.
     * <p>
     * NOTE: we DO NOT try to parse shell syntax here. The goal is simply
     * to strip obvious separators like ; & | and similar.
     */
    private static final Pattern COMMAND_DANGEROUS =
            Pattern.compile("[;&|`$(){}\"'<>\\r\\n§\u001B]");

    /**
     * Characters that are not allowed in file names on common filesystems.
     * We replace them with underscore.
     */
    private static final Pattern FILE_NAME_DANGEROUS =
            Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * Per-character blacklist for player names:
     * - allow Unicode letters (\p{L}), digits (\p{N}), underscore and dash
     * - strip everything else.
     * <p>
     * This keeps the logic permissive enough for International/Unicode names,
     * but prevents control characters and most weird symbols.
     */
    private static final Pattern PLAYER_NAME_STRIP =
            Pattern.compile("[^\\p{L}\\p{N}_\\-]");

    // =====================================================================
    // Sensitive data masking (for error messages)
    // =====================================================================

    /**
     * Basic masking for tokens/passwords/keys in error messages.
     * Example: "password=abc123" -> "password=[HIDDEN]"
     */
    private static final Pattern ERROR_MASK = Pattern.compile(
            "(password|token|key|secret|api[_-]?key)\\s*[=:]\\s*\\S+",
            Pattern.CASE_INSENSITIVE
    );

    // =====================================================================
    // Suspicious patterns (high-risk injection / RCE indicators)
    // =====================================================================

    /**
     * Patterns considered "highly suspicious".
     * <p>
     * IMPORTANT:
     * - We DO NOT block generic http:// or https:// here, because they are
     * common in legitimate error messages and logs.
     * - We DO block things like:
     * javascript:
     * <script> / </script>
     * eval( / exec( / system(
     * cmd.exe / powershell / /bin/sh / /bin/bash
     * chmod / wget / curl / nc - / python -c
     * ${jndi:...}
     * file://
     * Some forms of hex-escaped/unicode-escaped sequences.
     * <p>
     * This is NOT a perfect RCE detector, but catches many obvious attempts.
     */
    private static final Pattern SUSPICIOUS_PATTERNS = Pattern.compile(
            "(javascript:|data:text|<script|</script|eval\\(|exec\\(|system\\(|" +
                    "cmd\\.exe|powershell(\\.exe)?|/bin/sh|/bin/bash|chmod\\s|wget\\s|" +
                    "curl\\s|\\bnc\\s+-|python\\s+-c|\\$\\{jndi:|file://|" +
                    "x[a-f0-9]{2}|u[a-f0-9]{4}|\\\\u\\{[a-f0-9]{1,6}})",
            Pattern.CASE_INSENSITIVE
    );

    // =====================================================================
    // Sanitization types
    // =====================================================================

    public enum SanitizeType {
        /**
         * Player name: Unicode letters, digits, underscore, dash.
         */
        PLAYER_NAME,
        /**
         * Minecraft / console command constructed by plugin templates.
         */
        COMMAND,
        /**
         * IPv4 or IPv6 address.
         */
        IP_ADDRESS,
        /**
         * File name relative to plugin data folder.
         */
        FILE_NAME,
        /**
         * Tag-like string: GitHub tag, plugin version, etc.
         */
        TAG_NAME,
        /**
         * A generic version string (same rules as TAG_NAME).
         */
        VERSION,
        /**
         * Log message (for logger + log file).
         */
        LOG_MESSAGE,
        /**
         * Error message (stack traces, exceptions, etc.).
         */
        ERROR_MESSAGE,
        /**
         * Country name from external API.
         */
        COUNTRY,
        /**
         * Generic external API response we want to log safely.
         */
        API_RESPONSE
    }

    // =====================================================================
    // Public API
    // =====================================================================

    /**
     * Sanitize a string according to the given type with a default length limit.
     */
    public static String sanitize(@Nullable String input, SanitizeType type) {
        return sanitize(input, type, getDefaultMaxLength(type));
    }

    /**
     * Sanitize a string according to the given type with an explicit length limit.
     * <p>
     * Common steps:
     * 1) If input is null -> return type-specific default.
     * 2) Strip ANSI escape codes and trim.
     * 3) Apply type-specific sanitizer (no truncation inside).
     * 4) If result is empty -> return type-specific default.
     * 5) If result exceeds maxLength -> truncate and append "..." (if possible).
     */
    public static String sanitize(@Nullable String input, SanitizeType type, int maxLength) {
        if (input == null) {
            return getDefaultValue(type);
        }

        // Step 1: remove ANSI and trim
        String result = ANSI_ESCAPE.matcher(input).replaceAll("").trim();

        if (result.isEmpty()) {
            return getDefaultValue(type);
        }

        // Step 2: type-specific logic (without truncation)
        result = switch (type) {
            case PLAYER_NAME -> sanitizePlayerName(result);
            case COMMAND -> sanitizeCommand(result);
            case IP_ADDRESS -> sanitizeIpAddress(result);
            case FILE_NAME -> sanitizeFileName(result);
            case TAG_NAME -> sanitizeVersionLike(result);
            case VERSION -> sanitizeVersionLike(result);
            case LOG_MESSAGE -> sanitizeLogMessage(result);
            case ERROR_MESSAGE -> sanitizeErrorMessage(result);
            case COUNTRY -> sanitizeCountry(result);
            case API_RESPONSE -> sanitizeApiResponse(result);
        };

        // Step 3: empty result -> default value
        if (result == null || result.isEmpty()) {
            result = getDefaultValue(type);
        }

        // Step 4: truncate to maxLength (single place where we do that)
        return truncate(result, maxLength);
    }

    /**
     * Check if a raw input string contains suspicious patterns.
     * This is used inside the sanitizers for LOG_MESSAGE, ERROR_MESSAGE,
     * API_RESPONSE and VERSION/TAG_NAME.
     */
    public static boolean containsSuspiciousPatterns(@Nullable String input) {
        if (input == null) return false;
        return SUSPICIOUS_PATTERNS.matcher(input).find();
    }

    // =====================================================================
    // Type-specific sanitizers (no truncation here)
    // =====================================================================

    /**
     * Player name sanitizer.
     * <p>
     * Strategy:
     * - Remove any character that is NOT a Unicode letter, digit, underscore or dash.
     * - If the result is empty, caller will replace with default ("player").
     * <p>
     * This allows International/Unicode nicknames and long names, but strips control
     * characters and weird symbols.
     */
    private static String sanitizePlayerName(String input) {
        return PLAYER_NAME_STRIP.matcher(input).replaceAll("");
    }

    /**
     * Command sanitizer.
     * <p>
     * Strategy:
     * - Strip characters that can easily build dangerous shell constructs
     * (; & | ` $ ( ) { } " ' < > newlines, §, ESC).
     * <p>
     * NOTE:
     * - We deliberately do NOT block Unicode / non-ASCII here.
     * International commands like "/принять" will work fine.
     */
    private static String sanitizeCommand(String input) {
        return COMMAND_DANGEROUS.matcher(input).replaceAll("");
    }

    /**
     * IP address sanitizer.
     * <p>
     * Strategy:
     * - Strict whitelist: if the string is NOT a valid IPv4 or IPv6,
     * return a generic "invalid" marker.
     */
    private static String sanitizeIpAddress(String input) {
        String trimmed = input.trim();
        if (IPV4_SAFE.matcher(trimmed).matches() || IPV6_SAFE.matcher(trimmed).matches()) {
            return trimmed;
        }
        return getDefaultValue(SanitizeType.IP_ADDRESS); // "invalid"
    }

    /**
     * File name sanitizer.
     * <p>
     * Strategy:
     * - Replace filesystem-dangerous characters with "_".
     * - Caller is responsible for keeping the path inside plugin data folder.
     */
    private static String sanitizeFileName(String input) {
        String cleaned = FILE_NAME_DANGEROUS.matcher(input).replaceAll("_").trim();
        if (cleaned.isEmpty()) {
            return getDefaultValue(SanitizeType.FILE_NAME); // "file"
        }
        return cleaned;
    }

    /**
     * Version / tag sanitizer (TAG_NAME and VERSION).
     * <p>
     * Strategy:
     * - If string contains suspicious patterns -> "unknown".
     * - If it does NOT match VERSION_SAFE whitelist -> "unknown".
     * - Otherwise return the trimmed value.
     */
    private static String sanitizeVersionLike(String input) {
        String trimmed = input.trim();

        if (containsSuspiciousPatterns(trimmed)) {
            return getDefaultValue(SanitizeType.VERSION); // "unknown"
        }

        if (!VERSION_SAFE.matcher(trimmed).matches()) {
            return getDefaultValue(SanitizeType.VERSION); // "unknown"
        }

        return trimmed;
    }

    /**
     * Log message sanitizer.
     * <p>
     * Strategy:
     * - If message looks like an injection / RCE attempt, block completely.
     * - Otherwise:
     * * remove control characters
     * * normalize whitespace
     */
    private static String sanitizeLogMessage(String input) {
        if (containsSuspiciousPatterns(input)) {
            return "[BLOCKED: SUSPICIOUS PATTERN]";
        }

        String sanitized = CONTROL_CHARS.matcher(input).replaceAll(" ");
        sanitized = WHITESPACE_RUN.matcher(sanitized).replaceAll(" ").trim();
        return sanitized;
    }

    /**
     * Error message sanitizer.
     * <p>
     * Strategy:
     * - Mask obvious sensitive data (password=..., token=..., etc.).
     * - If the masked message still looks suspicious -> block it.
     * - Otherwise remove control chars and normalize whitespace.
     */
    private static String sanitizeErrorMessage(String input) {
        String masked = ERROR_MASK.matcher(input).replaceAll("$1=[HIDDEN]");

        if (containsSuspiciousPatterns(masked)) {
            return "[BLOCKED: SUSPICIOUS PATTERN]";
        }

        String sanitized = CONTROL_CHARS.matcher(masked).replaceAll(" ");
        sanitized = WHITESPACE_RUN.matcher(sanitized).replaceAll(" ").trim();
        return sanitized;
    }

    /**
     * Country name sanitizer.
     * <p>
     * Strategy:
     * - Remove control characters and normalize whitespace.
     * - No suspicious pattern check here (country comes from a narrow API).
     */
    private static String sanitizeCountry(String input) {
        String sanitized = CONTROL_CHARS.matcher(input).replaceAll(" ");
        sanitized = WHITESPACE_RUN.matcher(sanitized).replaceAll(" ").trim();
        if (sanitized.isEmpty()) {
            return getDefaultValue(SanitizeType.COUNTRY); // "Unknown"
        }
        return sanitized;
    }

    /**
     * Generic API response sanitizer.
     * <p>
     * Strategy:
     * - If response looks like an injection / RCE attempt -> block it.
     * - Otherwise strip control chars and normalize whitespace.
     */
    private static String sanitizeApiResponse(String input) {
        if (containsSuspiciousPatterns(input)) {
            return "[BLOCKED: SUSPICIOUS PATTERN]";
        }

        String sanitized = CONTROL_CHARS.matcher(input).replaceAll(" ");
        sanitized = WHITESPACE_RUN.matcher(sanitized).replaceAll(" ").trim();
        return sanitized;
    }

    // =====================================================================
    // Helpers: defaults, max length, truncation
    // =====================================================================

    private static String getDefaultValue(SanitizeType type) {
        return switch (type) {
            case PLAYER_NAME -> "player";
            case COMMAND -> "";
            case IP_ADDRESS -> "invalid";
            case FILE_NAME -> "file";
            case LOG_MESSAGE, API_RESPONSE -> "[BLOCKED: SUSPICIOUS PATTERN]";
            case ERROR_MESSAGE -> "[ERROR]";
            case COUNTRY -> "Unknown";
            case TAG_NAME, VERSION -> "unknown";
        };
    }

    private static int getDefaultMaxLength(SanitizeType type) {
        return switch (type) {
            case PLAYER_NAME -> 64;
            case COMMAND -> 256;
            case IP_ADDRESS -> 45;   // IPv6 max 39, plus small margin
            case FILE_NAME -> 100;
            case TAG_NAME -> 40;
            case LOG_MESSAGE -> 200;
            case ERROR_MESSAGE -> 500;
            case COUNTRY -> 60;
            case VERSION -> 40;
            case API_RESPONSE -> 500;
        };
    }

    /**
     * Truncate string to maxLength and append "..." when possible.
     * If maxLength <= 0, treat as no truncation (but we always pass sane values).
     */
    private static String truncate(String input, int maxLength) {
        int max = Math.max(0, maxLength);
        if (max == 0 || input.length() <= max) {
            return input;
        }

        if (max <= 3) {
            // No room for "..." — just hard cut
            return input.substring(0, max);
        }

        return input.substring(0, max - 3) + "...";
    }
}