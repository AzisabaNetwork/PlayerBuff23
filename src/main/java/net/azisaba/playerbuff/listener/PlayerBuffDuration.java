package net.azisaba.playerbuff.listener;

import net.azisaba.playerbuff.PlayerBuff;
import net.azisaba.playerbuff.PlayerBuffSetArmor;
import net.azisaba.playerbuff.PlayerBuffSetArmorToughness;
import net.azisaba.playerbuff.PlayerBuffSetDamage;
import net.azisaba.playerbuff.PlayerBuffSetHealth;
import net.azisaba.playerbuff.PlayerBuffSetSpeed;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBuffDuration implements Listener {

    private static PlayerBuff plugin;
    private static final Map<String, BukkitTask> BUFF_TASKS = new HashMap<>();
    private static final Map<String, Long> BUFF_EXPIRES_AT = new HashMap<>();

    public PlayerBuffDuration(PlayerBuff plugin) {
        PlayerBuffDuration.plugin = plugin;
    }

    public static void applyTimedBuff(LivingEntity entity, BuffType buffType, double level, long durationSeconds, DurationMode durationMode) {
        if (entity == null || durationSeconds <= 0L) {
            return;
        }

        UUID uuid = entity.getUniqueId();
        String key = getBuffKey(uuid, buffType);
        long now = System.currentTimeMillis();
        long currentExpiresAt = Math.max(BUFF_EXPIRES_AT.getOrDefault(key, 0L), now);
        long expiresAt;
        if (durationMode == DurationMode.REPLACE) {
            expiresAt = now + (durationSeconds * 1000L);
        } else {
            expiresAt = currentExpiresAt + (durationSeconds * 1000L);
        }
        BukkitTask previousTask = BUFF_TASKS.remove(key);
        if (previousTask != null) {
            previousTask.cancel();
        }

        if (hasBuff(entity, buffType)) {
            removeBuff(entity, buffType);
        }
        addBuff(entity, buffType, level);

        BUFF_EXPIRES_AT.put(key, expiresAt);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity target = getLatestEntity(uuid);
                if (target == null) {
                    BUFF_TASKS.remove(key);
                    BUFF_EXPIRES_AT.remove(key);
                    cancel();
                    return;
                }

                Long trackedExpiresAt = BUFF_EXPIRES_AT.get(key);
                if (trackedExpiresAt == null) {
                    BUFF_TASKS.remove(key);
                    cancel();
                    return;
                }

                long remainingMillis = trackedExpiresAt - System.currentTimeMillis();
                if (remainingMillis <= 0L) {
                    if (hasBuff(target, buffType)) {
                        removeBuff(target, buffType);
                    }
                    BUFF_TASKS.remove(key);
                    BUFF_EXPIRES_AT.remove(key);
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        BUFF_TASKS.put(key, task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearAllBuffs(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> clearAllBuffs(player), 20L);
    }

    private static void clearAllBuffs(LivingEntity entity) {
        if (entity == null) {
            return;
        }

        for (BuffType buffType : BuffType.values()) {
            if (hasBuff(entity, buffType)) {
                removeBuff(entity, buffType);
            }

            String key = getBuffKey(entity.getUniqueId(), buffType);
            BukkitTask task = BUFF_TASKS.remove(key);
            if (task != null) {
                task.cancel();
            }
            BUFF_EXPIRES_AT.remove(key);
        }
    }

    private static boolean hasBuff(LivingEntity entity, BuffType buffType) {
        switch (buffType) {
            case HEALTH:
                return PlayerBuffSetHealth.hasBuffHealth(entity);
            case DAMAGE:
                return PlayerBuffSetDamage.hasBuffDamage(entity);
            case ARMOR:
                return PlayerBuffSetArmor.hasBuffArmor(entity);
            case TOUGHNESS:
                return PlayerBuffSetArmorToughness.hasBuffArmorToughness(entity);
            case SPEED:
                return PlayerBuffSetSpeed.hasBuffSpeed(entity);
            default:
                return false;
        }
    }

    private static void addBuff(LivingEntity entity, BuffType buffType, double level) {
        switch (buffType) {
            case HEALTH:
                PlayerBuffSetHealth.addHealthAttributes(entity, level);
                return;
            case DAMAGE:
                PlayerBuffSetDamage.addDamageAttributes(entity, level);
                return;
            case ARMOR:
                PlayerBuffSetArmor.addArmorAttributes(entity, level);
                return;
            case TOUGHNESS:
                PlayerBuffSetArmorToughness.addArmorToughnessAttributes(entity, level);
                return;
            case SPEED:
                PlayerBuffSetSpeed.addSpeedAttributes(entity, level);
                return;
            default:
        }
    }

    private static void removeBuff(LivingEntity entity, BuffType buffType) {
        switch (buffType) {
            case HEALTH:
                PlayerBuffSetHealth.removeHealthAttributes(entity);
                return;
            case DAMAGE:
                PlayerBuffSetDamage.removeDamageAttributes(entity);
                return;
            case ARMOR:
                PlayerBuffSetArmor.removeArmorAttributes(entity);
                return;
            case TOUGHNESS:
                PlayerBuffSetArmorToughness.removeArmorToughnessAttributes(entity);
                return;
            case SPEED:
                PlayerBuffSetSpeed.removeSpeedAttributes(entity);
                return;
            default:
        }
    }

    private static String getBuffKey(UUID entityId, BuffType buffType) {
        return entityId.toString() + ":" + buffType.name();
    }

    private static LivingEntity getLatestEntity(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player;
    }

    public enum BuffType {
        HEALTH,
        DAMAGE,
        ARMOR,
        TOUGHNESS,
        SPEED;

        public static BuffType fromString(String type) {
            if (type == null) {
                return null;
            }

            switch (type.toUpperCase()) {
                case "HEALTH":
                    return HEALTH;
                case "DAMAGE":
                    return DAMAGE;
                case "ARMOR":
                    return ARMOR;
                case "TOUGHNESS":
                    return TOUGHNESS;
                case "SPEED":
                    return SPEED;
                default:
                    return null;
            }
        }
    }

    public enum DurationMode {
        ADD,
        REPLACE;

        public static DurationMode fromString(String mode) {
            if (mode == null) {
                return null;
            }

            switch (mode.toUpperCase()) {
                case "ADD":
                    return ADD;
                case "REPLACE":
                    return REPLACE;
                default:
                    return null;
            }
        }
    }
}
