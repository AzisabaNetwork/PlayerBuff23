package net.azisaba.playerbuff23.listener;

import net.azisaba.playerbuff23.PlayerBuff23;
import net.azisaba.playerbuff23.PlayerBuffSetArmor;
import net.azisaba.playerbuff23.PlayerBuffSetArmorToughness;
import net.azisaba.playerbuff23.PlayerBuffSetDamage;
import net.azisaba.playerbuff23.PlayerBuffSetHealth;
import net.azisaba.playerbuff23.PlayerBuffSetSpeed;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBuffDuration implements Listener {

    private static PlayerBuff23 plugin;
    private static final Map<String, Long> BUFF_EXPIRES_AT = new HashMap<>();

    public PlayerBuffDuration(PlayerBuff23 plugin) {
        PlayerBuffDuration.plugin = plugin;
    }

    public static void applyTimedBuff(LivingEntity entity, BuffType buffType, double level, long durationSeconds) {
        if (entity == null || durationSeconds <= 0L) {
            return;
        }

        if (hasBuff(entity, buffType)) {
            removeBuff(entity, buffType);
        }
        addBuff(entity, buffType, level);

        long durationTicks = durationSeconds * 20L;
        long expiresAtMillis = System.currentTimeMillis() + (durationSeconds * 1000L);
        String key = getBuffKey(entity.getUniqueId(), buffType);

        BUFF_EXPIRES_AT.put(key, expiresAtMillis);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Long currentTrackedExpiry = BUFF_EXPIRES_AT.get(key);
            if (currentTrackedExpiry == null || currentTrackedExpiry.longValue() != expiresAtMillis) {
                return;
            }

            if (hasBuff(entity, buffType)) {
                removeBuff(entity, buffType);
            }
            BUFF_EXPIRES_AT.remove(key);
        }, durationTicks);
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
            BUFF_EXPIRES_AT.remove(getBuffKey(entity.getUniqueId(), buffType));
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
}
