package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Generic, low-level argument types for the command framework.
 * <p>
 * SECURITY NOTE:
 * - These types perform only basic validation (null/empty/length/format).
 * - They DO NOT use SecurityUtils or perform context-aware sanitization.
 * - Values returned from ArgumentType are considered "raw" and may still
 * contain untrusted user input.
 * <p>
 * Sanitization is applied later, at the boundaries:
 * - when values are logged or shown to players,
 * - when they are inserted into external services (HTTP, database, etc.),
 * - or when they are used to construct commands/templates.
 * <p>
 * This keeps ArgumentType reusable and prevents accidental modification of
 * values that are used as exact keys (e.g. player names for Bukkit.getPlayerExact).
 */
public abstract class ArgumentType<T> {
    public abstract T parse(String input) throws ArgumentParseException;

    public abstract String getName();

    public static final ArgumentType<String> STRING = new ArgumentType<>() {
        @Override
        public String parse(String input) throws ArgumentParseException {
            if (input == null || input.trim().isEmpty()) {
                throw new ArgumentParseException("Input cannot be empty");
            }
            if (input.length() > 128) {
                throw new ArgumentParseException("Input too long (max 128 characters, got " + input.length() + ")");
            }
            return input;
        }

        @Override
        public String getName() {
            return "string";
        }
    };

    public static final ArgumentType<Integer> INTEGER = new ArgumentType<>() {
        @Override
        public Integer parse(String input) throws ArgumentParseException {
            if (input == null || input.trim().isEmpty()) {
                throw new ArgumentParseException("Input cannot be empty");
            }
            if (input.length() > 10) {
                throw new ArgumentParseException("Number too long");
            }
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("Invalid number format: '" + input + "'");
            }
        }

        @Override
        public String getName() {
            return "integer";
        }
    };

    public static final ArgumentType<Player> PLAYER = new ArgumentType<>() {
        @Override
        public Player parse(String input) throws ArgumentParseException {
            if (input == null || input.trim().isEmpty()) {
                throw new ArgumentParseException("Player name cannot be empty");
            }

            String trimmed = input.trim();

            // More flexible validation for pirate servers with non-ASCII names
            if (!isValidPlayerName(trimmed)) {
                throw new ArgumentParseException("Invalid player name format: '" + trimmed + "'");
            }

            Player player = Bukkit.getPlayerExact(trimmed);
            if (player == null) {
                throw new ArgumentParseException("Player '" + trimmed + "' not found or offline");
            }
            return player;
        }

        private boolean isValidPlayerName(String name) {
            // More flexible validation for pirate servers
            // Block only obviously dangerous characters, allow international names
            if (name.isEmpty() || name.length() > 64) {
                return false;
            }

            // Block dangerous characters but allow international characters
            // Block: < > & § (MiniMessage and color code chars)
            // Block: control characters except space
            for (char c : name.toCharArray()) {
                if (c == '<' || c == '>' || c == '&' || c == '§') {
                    return false; // Block MiniMessage/color chars
                }
                if (Character.isISOControl(c)) {
                    return false; // Block control characters
                }
            }

            return true;
        }

        @Override
        public String getName() {
            return "player";
        }
    };

    public static class ArgumentParseException extends Exception {
        public ArgumentParseException(String message) {
            super(message);
        }
    }
}