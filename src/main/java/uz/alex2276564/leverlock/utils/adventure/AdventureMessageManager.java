package uz.alex2276564.leverlock.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        String processedMessage = StringUtils.processEscapeSequences(message);
        //noinspection PatternValidation
        return miniMessage.deserialize(processedMessage,
                TagResolver.resolver(Placeholder.unparsed(placeholder, replacement)));
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        String processedMessage = StringUtils.processEscapeSequences(message);
        TagResolver.Builder builder = TagResolver.builder();

        //noinspection PatternValidation
        placeholders.forEach((key, value) ->
                builder.resolver(Placeholder.unparsed(key, value)));

        return miniMessage.deserialize(processedMessage, builder.build());
    }

    @Override
    public @NotNull Component parseWithTrustedPlaceholders(@NotNull String message, @NotNull Map<String, String> trustedPlaceholders) {
        String processedMessage = StringUtils.processEscapeSequences(message);
        TagResolver.Builder builder = TagResolver.builder();

        // ⚠️ WARNING - allows MiniMessage tags in values!
        //noinspection PatternValidation
        trustedPlaceholders.forEach((key, value) ->
                builder.resolver(Placeholder.parsed(key, value)));

        return miniMessage.deserialize(processedMessage, builder.build());
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
            // For console - strip tags and send as plain text
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message,
                            @NotNull String placeholder, @NotNull String replacement) {
        if (sender instanceof Player player) {
            player.sendMessage(parse(message, placeholder, replacement));
        } else {
            Component parsed = parse(message, placeholder, replacement);
            String plainText = PlainTextComponentSerializer.plainText().serialize(parsed);
            sender.sendMessage(plainText);
        }
    }
}