package uz.alex2276564.leverlock.config.configs.messagesconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.HashSet;
import java.util.Set;

public class MessagesConfig extends OkaeriConfig {

    @Comment("# ================================================================")
    @Comment("# 💬 Messages Configuration")
    @Comment("# ================================================================")
    @Comment("# 🎨 TEXT FORMATTING:")
    @Comment("# • Full MiniMessage support on Paper 1.18+")
    @Comment("# • Automatic fallback to legacy colors on older versions")
    @Comment("# • Examples: <red>Error!</red>, <green>Success!</green>")
    @Comment("# • Advanced: gradients, hover effects, click events")
    @Comment("# • Web editor: https://webui.advntr.dev/")
    @Comment("#")
    @Comment("# 🌍 LOCALIZATION:")
    @Comment("# • This plugin doesn't include built-in multi-language support")
    @Comment("# • For multiple languages, use Triton plugin:")
    @Comment("#   → https://www.spigotmc.org/resources/triton.30331/")
    @Comment("# ================================================================")
    @Comment("")

    @Comment("# ================================================================")
    @Comment("# 🔇 MESSAGE CONTROL SYSTEM")
    @Comment("# ================================================================")
    @Comment("# You can selectively disable individual messages by adding their")
    @Comment("# keys to the list below. This is useful for customizing user")
    @Comment("# experience without editing every message.")
    @Comment("#")
    @Comment("# 📝 HOW TO USE:")
    @Comment("# 1. Find the message you want to disable in this config")
    @Comment("# 2. Copy its full path using dot notation")
    @Comment("# 3. Add the path to disabledKeys list below")
    @Comment("# 4. Reload the plugin")
    @Comment("#")
    @Comment("# 🎯 EXAMPLES:")
    @Comment("# To disable specific command feedback:")
    @Comment("# - 'commands.reload.success'")
    @Comment("# - 'commands.help.header'")
    @Comment("#")
    @Comment("# To disable general system messages:")
    @Comment("# - 'general.noSpawnFound'")
    @Comment("# - 'general.systemDisabled'")
    @Comment("#")
    @Comment("# ⚠️ IMPORTANT NOTES:")
    @Comment("# • Keys are case-sensitive and must match exactly")
    @Comment("# • This affects ALL recipients (players AND console)")
    @Comment("# • Some messages may be important for debugging")
    @Comment("# • Also keep in mind that some messages are hardcoded")
    @Comment("# • into the plugin logic and cannot be disabled")
    @Comment("# • Do NOT use empty strings (\"\") to disable messages")
    @Comment("# • Do NOT delete message entries - use this system instead")
    @Comment("#")
    @Comment("# 🔍 FINDING KEYS:")
    @Comment("# Structure follows: section.subsection.messageKey")
    @Comment("# Check the organization below to find the correct path")
    @Comment("# ================================================================")
    @Comment("")
    public Set<String> disabledKeys = new HashSet<>();

    @Comment("")
    @Comment("Command messages")
    public CommandsSection commands = new CommandsSection();

    @Comment("")
    @Comment("General messages")
    public GeneralSection general = new GeneralSection();

    public static class CommandsSection extends OkaeriConfig {
        @Comment("Help command messages")
        public HelpSection help = new HelpSection();

        @Comment("")
        @Comment("Reload command messages")
        public ReloadSection reload = new ReloadSection();

        public static class HelpSection extends OkaeriConfig {
            @Comment("Help command header")
            public String header = "<gold>=== LeverLock Help ===";

            @Comment("Reload command help line")
            public String reloadLine = "<yellow>/leverlock reload <type> <gray>- Reload the plugin configuration";

            @Comment("Help command help line")
            public String helpLine = "<yellow>/leverlock help <gray>- Show this help message";
        }

        public static class ReloadSection extends OkaeriConfig {
            @Comment("Reload success message. <type> = config type")
            public String success = "<green>LeverLock configuration successfully reloaded (<type>).";

            @Comment("Reload error message. <error> = error details")
            public String error = "<red>Failed to reload configuration: <error>";
        }
    }

    public static class GeneralSection extends OkaeriConfig {        @Comment("Message displayed when a player tries to interact with a lever too quickly")
        public String cooldown = "<red>You are interacting with the lever too quickly!";
    }
}