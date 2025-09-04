package uz.alex2276564.leverlock.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.config.LeverLockConfigManager;
import uz.alex2276564.leverlock.events.PlayerInteractWithLeverEvent;
import uz.alex2276564.leverlock.utils.adventure.MessageManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeverClickListener implements Listener {
    private final LeverLock plugin;
    private static final Material TARGET_BLOCK = Material.LEVER;
    private final Map<Player, Instant> cooldownMap = new ConcurrentHashMap<>();
    private final MessageManager messageManager;

    public PlayerLeverClickListener(LeverLock plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
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
        LeverLockConfigManager config = plugin.getConfigManager();

        Duration cooldownDuration = Duration.ofSeconds(config.getMainConfig().cooldown.duration);

        if (Duration.between(lastInteractionTime, currentTime).compareTo(cooldownDuration) < 0) {
            event.setCancelled(true);

            if (config.getMainConfig().notifications.sendCooldownMessage) {
                Component message = messageManager.parse(config.getMessagesConfig().general.cooldown);
                player.sendMessage(message);
            }
        } else {
            cooldownMap.put(player, currentTime);
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

        cooldownMap.entrySet().removeIf(entry ->
                !entry.getKey().isOnline() || Duration.between(entry.getValue(), now).compareTo(cooldownDuration) > 0
        );
    }
}