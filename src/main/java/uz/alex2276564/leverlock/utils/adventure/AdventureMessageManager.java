package uz.alex2276564.leverlock.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.utils.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AdventureMessageManager implements MessageManager {

    private final MiniMessage miniMessage;
    private Supplier<Set<String>> disabledKeysSupplier = Collections::emptySet;

    public AdventureMessageManager() {
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void configureDisabledKeysProvider(@NotNull Supplier<Set<String>> supplier) {
        this.disabledKeysSupplier = supplier;
    }

    private boolean isDisabled(String key) {
        if (key == null || key.isBlank()) return false;
        try {
            Set<String> s = disabledKeysSupplier.get();
            return s != null && s.contains(key);
        } catch (Throwable ignored) {
            return false;
        }
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
        placeholders.forEach((key, value) -> builder.resolver(Placeholder.unparsed(key, value)));
        return miniMessage.deserialize(processedMessage, builder.build());
    }

    @Override
    public @NotNull Component parseWithTrustedPlaceholders(@NotNull String message, @NotNull Map<String, String> trustedPlaceholders) {
        String processedMessage = StringUtils.processEscapeSequences(message);
        TagResolver.Builder builder = TagResolver.builder();

        // ⚠️ WARNING - allows MiniMessage tags in values!
        //noinspection PatternValidation
        trustedPlaceholders.forEach((key, value) -> builder.resolver(Placeholder.parsed(key, value)));
        return miniMessage.deserialize(processedMessage, builder.build());
    }

    @Override
    public @NotNull String stripTags(@NotNull String message) {
        return miniMessage.stripTags(message);
    }

    // ========= helpers =========

    private void sendToPlayer(Player player, Component component) {
        var runner = LeverLock.getInstance().getRunner();
        // On Paper this returns true → send immediately; on Folia checks region-thread ownership
        if (runner.isOwnedByCurrentRegion(player)) {
            player.sendMessage(component);
        } else {
            runner.runAtEntity(player, () -> player.sendMessage(component));
        }
    }

    private void sendToConsole(CommandSender sender, Component component) {
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        sender.sendMessage(plain);
    }

    // ========= non-keyed =========

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        sendToPlayer(player, parse(message));
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        sendToPlayer(player, parse(message, placeholder, replacement));
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message));
        } else {
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message, placeholder, replacement));
        } else {
            sendToConsole(sender, parse(message, placeholder, replacement));
        }
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message, @NotNull Map<String, String> placeholders) {
        sendToPlayer(player, parse(message, placeholders));
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull Map<String, String> placeholders) {
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message, placeholders));
        } else {
            sendToConsole(sender, parse(message, placeholders));
        }
    }

    // ========= keyed =========

    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message) {
        if (isDisabled(key)) return;
        sendToPlayer(player, parse(message));
    }

    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        if (isDisabled(key)) return;
        sendToPlayer(player, parse(message, placeholder, replacement));
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message) {
        if (isDisabled(key)) return;
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message));
        } else {
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        if (isDisabled(key)) return;
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message, placeholder, replacement));
        } else {
            sendToConsole(sender, parse(message, placeholder, replacement));
        }
    }

    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message, @NotNull Map<String, String> placeholders) {
        if (isDisabled(key)) return;
        sendToPlayer(player, parse(message, placeholders));
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message, @NotNull Map<String, String> placeholders) {
        if (isDisabled(key)) return;
        if (sender instanceof Player player) {
            sendToPlayer(player, parse(message, placeholders));
        } else {
            sendToConsole(sender, parse(message, placeholders));
        }
    }
}