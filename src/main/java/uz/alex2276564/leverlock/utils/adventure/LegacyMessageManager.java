package uz.alex2276564.leverlock.utils.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.leverlock.utils.StringUtils;

import java.util.*;
import java.util.function.Supplier;

public class LegacyMessageManager implements MessageManager {

    // Supported base tags -> legacy codes
    private static final Map<String, String> COLOR = Map.ofEntries(
            Map.entry("black", "0"),
            Map.entry("dark_blue", "1"),
            Map.entry("dark_green", "2"),
            Map.entry("dark_aqua", "3"),
            Map.entry("dark_red", "4"),
            Map.entry("dark_purple", "5"),
            Map.entry("gold", "6"),
            Map.entry("gray", "7"),
            Map.entry("dark_gray", "8"),
            Map.entry("blue", "9"),
            Map.entry("green", "a"),
            Map.entry("aqua", "b"),
            Map.entry("red", "c"),
            Map.entry("light_purple", "d"),
            Map.entry("yellow", "e"),
            Map.entry("white", "f")
    );
    private static final Map<String, String> STYLE = Map.ofEntries(
            Map.entry("obfuscated", "k"),
            Map.entry("bold", "l"),
            Map.entry("strikethrough", "m"),
            Map.entry("underlined", "n"),
            Map.entry("italic", "o")
    );

    private Supplier<Set<String>> disabledKeysSupplier = Collections::emptySet;

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
        String processed = StringUtils.processEscapeSequences(message);
        String legacy = toLegacy(processed);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        String safe = escapeForLegacy(replacement);
        // replace both "<placeholder>"
        String processed = message
                .replace("<" + placeholder + ">", safe);
        return parse(processed);
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        String processed = message;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            String safe = escapeForLegacy(e.getValue());
            processed = processed
                    .replace("<" + e.getKey() + ">", safe);
        }
        return parse(processed);
    }

    @Override
    public @NotNull Component parseWithTrustedPlaceholders(@NotNull String message, @NotNull Map<String, String> trusted) {
        // Trusted: do NOT escape user input
        String processed = message;
        for (Map.Entry<String, String> e : trusted.entrySet()) {
            processed = processed.replace(e.getKey(), e.getValue());
        }
        return parse(processed);
    }

    @Override
    public @NotNull String stripTags(@NotNull String message) {
        // Convert <br>/<newline> to line breaks first
        StringBuilder out = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c != '<') {
                out.append(c);
                continue;
            }
            int end = message.indexOf('>', i + 1);
            if (end < 0) {
                // Broken tag, print '<' literally
                out.append(c);
                continue;
            }

            String tag = message.substring(i + 1, end).trim();     // original inside <>
            String lower = tag.toLowerCase(Locale.ROOT);     // case-insensitive checks
            i = end; // consume the tag

            // Newline shortcuts
            if ("br".equals(lower) || "newline".equals(lower)) {
                out.append('\n');
                continue;
            }

            // Reset, explicit closing tags, known colors/styles, or complex (hover/click/gradient/etc.)
            // → drop formatting-only tags for plain text output
            if ("reset".equals(lower)
                    || lower.startsWith("/")
                    || COLOR.containsKey(lower)
                    || STYLE.containsKey(lower)
                    || isComplex(lower)) {
                // skip tag (remove it from plain text)
                continue;
            }

            // Unknown tag → keep literally so placeholders like <player> are not lost
            out.append('<').append(tag).append('>');
        }

        return out.toString();
    }

    // non-keyed
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
        if (sender instanceof Player p) {
            sendMessage(p, message);
        } else {
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message,
                            @NotNull String placeholder, @NotNull String replacement) {
        if (sender instanceof Player p) {
            p.sendMessage(parse(message, placeholder, replacement));
        } else {
            String token = "<" + placeholder + ">";
            String processed = message.replace(token, escapeForLegacy(replacement));
            sender.sendMessage(stripTags(processed));
        }
    }

    // keyed
    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message) {
        if (isDisabled(key)) return;
        player.sendMessage(parse(message));
    }

    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        if (isDisabled(key)) return;
        player.sendMessage(parse(message, placeholder, replacement));
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message) {
        if (isDisabled(key)) return;
        if (sender instanceof Player p) {
            sendMessageKeyed(p, key, message);
        } else {
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        if (isDisabled(key)) return;
        if (sender instanceof Player p) {
            p.sendMessage(parse(message, placeholder, replacement));
        } else {
            String token = "<" + placeholder + ">";
            String processed = message.replace(token, escapeForLegacy(replacement));
            sender.sendMessage(stripTags(processed));
        }
    }

    @Override
    public void sendMessageKeyed(@NotNull Player player, String key, @NotNull String message, @NotNull Map<String, String> placeholders) {
        if (isDisabled(key)) return;
        player.sendMessage(parse(message, placeholders));
    }

    @Override
    public void sendMessageKeyed(@NotNull CommandSender sender, String key, @NotNull String message, @NotNull Map<String, String> placeholders) {
        if (isDisabled(key)) return;
        if (sender instanceof Player p) {
            p.sendMessage(parse(message, placeholders));
        } else {
            // For the console — plain text without tags
            String processed = message;
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String token = "<" + e.getKey() + ">";
                processed = processed.replace(token, escapeForLegacy(e.getValue()));
            }
            sender.sendMessage(stripTags(processed));
        }
    }

    // === MiniMessage-lite -> Legacy (stack-based) ===
    private String toLegacy(String msg) {
        StringBuilder out = new StringBuilder(msg.length() + 16);
        Deque<Tag> stack = new ArrayDeque<>();

        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c != '<') {
                out.append(c);
                continue;
            }
            int end = msg.indexOf('>', i + 1);
            if (end < 0) { // broken tag, just print '<'
                out.append(c);
                continue;
            }
            String tag = msg.substring(i + 1, end).trim(); // original inside <>
            i = end;

            String lower = tag.toLowerCase(Locale.ROOT);

            // newline shortcuts
            if (equalsAny(lower, "br", "newline")) {
                out.append('\n');
            }
            // reset
            else if ("reset".equals(lower)) {
                stack.clear();
                out.append("&r");
            }
            // closing tag
            else if (lower.startsWith("/")) {
                String name = lower.substring(1);
                if (!stack.isEmpty()) {
                    // remove one matching tag (top-most first)
                    Deque<Tag> tmp = new ArrayDeque<>();
                    boolean removed = false;
                    while (!stack.isEmpty()) {
                        Tag t = stack.pop();
                        if (!removed && t.name.equals(name)) {
                            removed = true; // drop this one
                        } else {
                            tmp.push(t);
                        }
                    }
                    // restore stack without removed tag
                    while (!tmp.isEmpty()) stack.push(tmp.pop());

                    if (removed) {
                        // re-emit all active codes (color first, then styles)
                        reemitStack(out, stack);
                    } else {
                        // unknown closer → keep literally
                        out.append('<').append(tag).append('>');
                    }
                } else {
                    // no active tags → keep literally
                    out.append('<').append(tag).append('>');
                }
            }
            // complex tags we ignore: gradient:..., hover:..., click:..., font:..., ...
            else if (isComplex(lower)) {
                // ignore formatting that can't be represented in legacy
            }
            // color
            else if (COLOR.containsKey(lower)) {
                removeFirstFromStack(stack, t -> t.type == TagType.COLOR); // color replaces color
                stack.push(new Tag(TagType.COLOR, lower, "&" + COLOR.get(lower)));
                reemitStack(out, stack);
            }
            // style
            else if (STYLE.containsKey(lower) && !containsTag(stack, lower)) {
                stack.push(new Tag(TagType.STYLE, lower, "&" + STYLE.get(lower)));
                reemitStack(out, stack);
            }
            // unknown tag -> keep literally (so placeholders like <player> are visible if not replaced earlier)
            else {
                out.append('<').append(tag).append('>');
            }
        }
        return out.toString();
    }

    private enum TagType { COLOR, STYLE }

    /**
     * @param code legacy code (&a, &l, etc.)
     */
    private record Tag(TagType type, String name, String code) {
    }

    private static void reemitStack(StringBuilder out, Deque<Tag> stack) {
        // rebuild active state: bottom-most color first, then all styles
        List<Tag> list = new ArrayList<>(stack); // iteration is top->bottom
        Tag lastColor = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).type == TagType.COLOR) { lastColor = list.get(i); break; }
        }
        if (lastColor != null) out.append(lastColor.code);
        for (int i = list.size() - 1; i >= 0; i--) {
            Tag t = list.get(i);
            if (t.type == TagType.STYLE) out.append(t.code);
        }
    }

    private static void removeFirstFromStack(Deque<Tag> stack, java.util.function.Predicate<Tag> p) {
        if (stack.isEmpty()) return;
        Deque<Tag> tmp = new ArrayDeque<>();
        boolean removed = false;
        while (!stack.isEmpty()) {
            Tag t = stack.pop();
            if (!removed && p.test(t)) { removed = true; continue; }
            tmp.push(t);
        }
        while (!tmp.isEmpty()) stack.push(tmp.pop());
    }

    private static boolean containsTag(Deque<Tag> stack, String name) {
        for (Tag t : stack) if (t.name.equals(name)) return true;
        return false;
    }

    private static boolean equalsAny(String s, String... arr) {
        for (String a : arr) if (a.equalsIgnoreCase(s)) return true;
        return false;
    }

    private static boolean isComplex(String tag) {
        // rough check for tags we don't support in legacy: gradient:..., hover:..., click:..., font:..., etc.
        int idx = tag.indexOf(':');
        if (idx <= 0) return false;
        String head = tag.substring(0, idx).toLowerCase(Locale.ROOT);
        return switch (head) {
            case "gradient", "hover", "click", "font", "insertion", "key", "lang", "selector", "score", "nbt" -> true;
            default -> false;
        };
    }

    private String escapeForLegacy(String input) {
        if (input == null) return "";
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') continue;
            // Avoid generating MiniMessage/legacy sequences accidentally
            switch (c) {
                case '<' -> out.append('‹');
                case '>' -> out.append('›');
                case '&' -> out.append('＆');
                case '§' -> { /* drop */ }
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}