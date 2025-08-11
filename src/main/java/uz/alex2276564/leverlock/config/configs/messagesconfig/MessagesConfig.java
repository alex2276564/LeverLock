package uz.alex2276564.leverlock.config.configs.messagesconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MessagesConfig extends OkaeriConfig {

    @Comment("# ================================================================")
    @Comment("# 📝 LeverLock Messages Configuration")
    @Comment("#")
    @Comment("# 💬 All messages support MiniMessage formatting!")
    @Comment("#     → Works best with Paper 1.18+")
    @Comment("#     → Older versions automatically fallback to legacy color formatting")
    @Comment("#     → Examples: <red>Error!</red>, <green>Success!</green>")
    @Comment("#     → Use gradients, hover effects, click events, etc.")
    @Comment("#     → Web editor: https://webui.advntr.dev/")
    @Comment("#")
    @Comment("# 🌍 LOCALIZATION NOTE:")
    @Comment("#     Direct localization is not supported in this plugin.")
    @Comment("#     If you need multi-language support, use Triton plugin:")
    @Comment("#     → https://www.spigotmc.org/resources/triton.30331/")
    @Comment("# ================================================================")
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