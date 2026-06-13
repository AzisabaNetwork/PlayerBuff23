package net.azisaba.playerbuff.listener;

import net.azisaba.playerbuff.PlayerBuff;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldDreamSystem implements Listener {

    private final PlayerBuff plugin;

    public WorldDreamSystem(PlayerBuff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LivingEntity entity = event.getPlayer();
        if (this.hasDreamWorldHealth(entity) && this.hasDreamWorldSpeed(entity)) {
            this.removeHealthAttributes(entity);
            this.removeSpeedAttributes(entity);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName2");
        if (!world.getName().equals(world1)) {
            this.removeHealthAttributes(entity);
        }
        this.removeSpeedAttributes(entity);
        if (!this.hasDreamWorldHealth(entity) && world.getName().equals(world1)) {
            this.addHealthAttributes(entity);
        }
        if (!this.hasDreamWorldSpeed(entity) && world.getName().equals(world1)) {
            this.addSpeedAttributes(entity);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName2");
        if (!world.getName().equals(world1)) {
            if (this.hasDreamWorldSpeed(entity)) {
                this.removeSpeedAttributes(entity);
            }
            if (this.hasDreamWorldHealth(entity)) {
                this.removeHealthAttributes(entity);
            }
        }
    }

    private void removeHealthAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for(AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.dream_health_scalar")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    private void removeSpeedAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            for(AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.dream_speed_scalar")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    private boolean hasDreamWorldHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.dream_health_scalar")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDreamWorldSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.dream_speed_scalar")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addHealthAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff.dream_health_scalar", 3.0F, AttributeModifier.Operation.ADD_SCALAR));
        }
    }

    private void addSpeedAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff.dream_speed_scalar", -0.5F, AttributeModifier.Operation.ADD_SCALAR));
        }
    }
}
