package uz.alex2276564.leverlock.config.configs.mainconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MainConfig extends OkaeriConfig {

    @Comment("# ================================================================")
    @Comment("# 📝 LeverLock Configuration")
    @Comment("# ================================================================")
    @Comment("")
    @Comment("Cooldown settings")
    public CooldownSection cooldown = new CooldownSection();

    @Comment("")
    @Comment("Cleanup settings")
    public CleanupSection cleanup = new CleanupSection();

    @Comment("")
    @Comment("Notification settings")
    public NotificationSection notifications = new NotificationSection();

    public static class CooldownSection extends OkaeriConfig {
        @Comment("Duration in seconds before a player can interact with a lever again")
        public int duration = 1;
    }

    public static class CleanupSection extends OkaeriConfig {
        @Comment("Interval in seconds for cleaning up the cooldown data (5 minutes by default)")
        public int interval = 300;
    }

    public static class NotificationSection extends OkaeriConfig {
        @Comment("Whether to send cooldown messages to players")
        @Comment("Set to false to disable cooldown notifications")
        public boolean sendCooldownMessage = true;
    }
}