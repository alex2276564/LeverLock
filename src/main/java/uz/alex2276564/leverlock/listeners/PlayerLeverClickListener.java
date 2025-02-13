package uz.alex2276564.leverlock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import uz.alex2276564.leverlock.events.PlayerInteractWithLeverEvent;
import uz.alex2276564.leverlock.task.Runner;
import uz.alex2276564.leverlock.utils.ConfigManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private final Runner runner;
    private static final Material TARGET_BLOCK = Material.LEVER;
    private final Map<Player, Instant> cooldownMap = new ConcurrentHashMap<>();

    public PlayerLeverClickListener(Runner runner) {
        this.runner = runner;
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

        if (Duration.between(lastInteractionTime, currentTime).compareTo(ConfigManager.getCooldownDuration()) < 0) {
            event.setCancelled(true);
            player.sendMessage(ConfigManager.getCooldownMessage());
        } else {
            cooldownMap.put(player, currentTime);
        }
    }

    private void startCleanupTask() {
        runner.runPeriodical(
            this::cleanupCooldowns, ConfigManager.getCleanupInterval(), ConfigManager.getCleanupInterval()
        );
    }

    private void cleanupCooldowns() {
        Instant now = Instant.now();
        cooldownMap.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() || Duration.between(entry.getValue(), now).compareTo(ConfigManager.getCooldownDuration()) > 0
        );
    }
}
