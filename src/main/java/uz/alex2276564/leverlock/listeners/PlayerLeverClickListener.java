package uz.alex2276564.leverlock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.config.LeverLockConfigManager;
import uz.alex2276564.leverlock.events.PlayerInteractWithLeverEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private final LeverLock plugin;
    private static final Material TARGET_BLOCK = Material.LEVER;
    private final Map<UUID, Instant> cooldownMap = new ConcurrentHashMap<>();

    public PlayerLeverClickListener(LeverLock plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    @EventHandler(
            ignoreCancelled = true
    )
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

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void on(PlayerInteractWithLeverEvent event) {
        Player player = event.getPlayer();
        Instant currentTime = Instant.now();
        Instant lastInteractionTime = cooldownMap.getOrDefault(player.getUniqueId(), Instant.EPOCH);
        LeverLockConfigManager config = plugin.getConfigManager();

        Duration cooldownDuration = Duration.ofSeconds(config.getMainConfig().cooldown.duration);

        if (Duration.between(lastInteractionTime, currentTime).compareTo(cooldownDuration) < 0) {
            event.setCancelled(true);

            String message = config.getMessagesConfig().general.cooldown;
            LeverLock.getInstance().getMessageManager().sendMessageKeyed(player, "general.cooldown", message);
        } else {
            cooldownMap.put(player.getUniqueId(), currentTime);
        }
    }

    private void startCleanupTask() {
        LeverLockConfigManager config = plugin.getConfigManager();
        long intervalTicks = config.getMainConfig().cleanup.interval * 20L;
        plugin.getRunner().runGlobalTimer(
                this::cleanupCooldowns, intervalTicks, intervalTicks
        );
    }

    private void cleanupCooldowns() {
        Instant now = Instant.now();
        LeverLockConfigManager config = plugin.getConfigManager();
        Duration cooldownDuration = Duration.ofSeconds(config.getMainConfig().cooldown.duration);

        cooldownMap.entrySet().removeIf(entry -> Duration.between(entry.getValue(), now).compareTo(cooldownDuration) > 0
        );
    }
}