package uz.alex2276564.leverlock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.utils.ConfigManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private final JavaPlugin plugin;
    private static final Material TARGET_BLOCK = Material.LEVER;

    private final Map<Player, Instant> cooldownMap = new ConcurrentHashMap<>();

    public PlayerLeverClickListener(LeverLock plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && clickedBlock.getType() == TARGET_BLOCK) {
            Instant currentTime = Instant.now();
            Instant lastInteractionTime = cooldownMap.getOrDefault(player, Instant.EPOCH);

            if (Duration.between(lastInteractionTime, currentTime).compareTo(ConfigManager.getCooldownDuration()) < 0) {
                event.setCancelled(true);
                player.sendMessage(ConfigManager.getCooldownMessage());
            } else {
                cooldownMap.put(player, currentTime);
            }
        }
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupCooldowns, ConfigManager.getCleanupInterval(), ConfigManager.getCleanupInterval());
    }

    private void cleanupCooldowns() {
        Instant now = Instant.now();
        cooldownMap.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() || Duration.between(entry.getValue(), now).compareTo(ConfigManager.getCooldownDuration()) > 0
        );
    }
}
