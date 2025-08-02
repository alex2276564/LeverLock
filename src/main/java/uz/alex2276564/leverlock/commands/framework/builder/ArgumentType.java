package uz.alex2276564.leverlock.commands.framework.builder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class ArgumentType<T> {
    public abstract T parse(String input) throws ArgumentParseException;
    public abstract String getName();

    public static final ArgumentType<String> STRING = new ArgumentType<>() {
        @Override
        public String parse(String input) {
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
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("'" + input + "' is not a valid number");
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
            Player player = Bukkit.getPlayer(input);
            if (player == null) {
                throw new ArgumentParseException("Player '" + input + "' not found or is offline");
            }
            return player;
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