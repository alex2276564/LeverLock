package uz.alex2276564.leverlock.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private Duration cooldownDuration;
    private static final Material TARGET_BLOCK = Material.LEVER;
    private long cleanupInterval;
    private String cooldownMessage;

    private final Map<Player, Instant> cooldownMap = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;

    public PlayerLeverClickListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cooldownDuration = Duration.ofSeconds(plugin.getConfig().getLong("cooldown.duration", 1));
        this.cleanupInterval = plugin.getConfig().getLong("cleanup.interval", 300) * 20; // convert to ticks
        this.cooldownMessage = plugin.getConfig().getString("message.cooldown", "§cYou are interacting with the lever too quickly!");
        startCleanupTask();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && clickedBlock.getType() == TARGET_BLOCK) {
            Instant currentTime = Instant.now();
            Instant lastInteractionTime = cooldownMap.getOrDefault(player, Instant.EPOCH);

            if (Duration.between(lastInteractionTime, currentTime).compareTo(cooldownDuration) < 0) {
                event.setCancelled(true);
                player.sendMessage(cooldownMessage);
            } else {
                cooldownMap.put(player, currentTime);
            }
        }
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupCooldowns, cleanupInterval, cleanupInterval);
    }

    private void cleanupCooldowns() {
        Instant now = Instant.now();
        cooldownMap.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() || Duration.between(entry.getValue(), now).compareTo(cooldownDuration) > 0
        );
    }

    public void reloadConfig() {
        this.cooldownDuration = Duration.ofSeconds(plugin.getConfig().getLong("cooldown.duration", 1));
        this.cleanupInterval = plugin.getConfig().getLong("cleanup.interval", 300) * 20;
        this.cooldownMessage = plugin.getConfig().getString("message.cooldown", "§cYou are interacting with the lever too quickly!");
    }
}
