package uz.alex2276564.leverlock.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.utils.StringUtils;

import java.util.Map;

public class AdventureMessageManager implements MessageManager {

    private final MiniMessage miniMessage;

    public AdventureMessageManager() {
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public @NotNull Component parse(@NotNull String message) {

        String processedMessage = StringUtils.processEscapeSequences(message);

        return miniMessage.deserialize(processedMessage);
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        return parse(message.replace(placeholder, replacement));
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        String processedMessage = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processedMessage = processedMessage.replace(entry.getKey(), entry.getValue());
        }
        return parse(processedMessage);
    }

    @Override
    public @NotNull String stripTags(@NotNull String message) {
        return miniMessage.stripTags(message);
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        player.sendMessage(parse(message));
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        player.sendMessage(parse(message, placeholder, replacement));
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) {
            sendMessage(player, message);
        } else {
            // For the console - strip tags
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        sendMessage(sender, message.replace(placeholder, replacement));
    }
}
