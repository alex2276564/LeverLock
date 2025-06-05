package uz.alex2276564.leverlock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.events.PlayerInteractWithLeverEvent;
import uz.alex2276564.leverlock.config.ConfigManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private final LeverLock plugin;
    private static final Material TARGET_BLOCK = Material.LEVER;
    private final Map<Player, Instant> cooldownMap = new ConcurrentHashMap<>();

    public PlayerLeverClickListener(LeverLock plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && clickedBlock.getType() == TARGET_BLOCK) {
            final PlayerInteractWithLeverEvent e = new PlayerInteractWithLeverEvent(player);
            Bukkit.getPluginManager().callEvent(e);
            if(e.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(PlayerInteractWithLeverEvent event) {
        Player player = event.getPlayer();
        Instant currentTime = Instant.now();
        Instant lastInteractionTime = cooldownMap.getOrDefault(player, Instant.EPOCH);
        ConfigManager config = plugin.getConfigManager();

        if (Duration.between(lastInteractionTime, currentTime).compareTo(config.getCooldownDuration()) < 0) {
            event.setCancelled(true);
            player.sendMessage(config.getCooldownMessage());
        } else {
            cooldownMap.put(player, currentTime);
        }
    }

    private void startCleanupTask() {
        ConfigManager config = plugin.getConfigManager();
        plugin.getRunner().runPeriodical(
                this::cleanupCooldowns, config.getCleanupInterval(), config.getCleanupInterval()
        );
    }

    private void cleanupCooldowns() {
        Instant now = Instant.now();
        ConfigManager config = plugin.getConfigManager();
        cooldownMap.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() || Duration.between(entry.getValue(), now).compareTo(config.getCooldownDuration()) > 0
        );
    }
}
