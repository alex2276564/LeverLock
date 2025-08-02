package uz.alex2276564.leverlock.utils.adventure;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MessageManager {

    /**
     * Parse MiniMessage string to Component
     */
    @NotNull Component parse(@NotNull String message);

    /**
     * Parse with single placeholder replacement
     */
    @NotNull Component parse(@NotNull String message, @NotNull String placeholder, @NotNull String replacement);

    /**
     * Parse with multiple placeholder replacements
     */
    @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders);

    /**
     * Strip MiniMessage tags and return plain text
     */
    @NotNull String stripTags(@NotNull String message);

    /**
     * Send parsed message to player
     */
    void sendMessage(@NotNull Player player, @NotNull String message);

    /**
     * Send parsed message with placeholder to player
     */
    void sendMessage(@NotNull Player player, @NotNull String message, @NotNull String placeholder, @NotNull String replacement);

    /**
     * Send message to CommandSender (Player or Console)
     * For players - sends parsed Component
     * For console - sends stripped plain text
     */
    void sendMessage(@NotNull CommandSender sender, @NotNull String message);

    /**
     * Send message to CommandSender with placeholder replacement
     */
    void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull String placeholder, @NotNull String replacement);
}
