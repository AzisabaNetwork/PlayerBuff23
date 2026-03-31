package net.azisaba.playerbuff23.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import net.azisaba.playerbuff23.PlayerBuff23;
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
import org.bukkit.inventory.ItemStack;

public class WorldDreamTest implements Listener {

    private final PlayerBuff23 plugin;

    public WorldDreamTest(PlayerBuff23 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LivingEntity entity = event.getPlayer();
        if (this.hasTempDamage(entity)) {
            removeAttributes(entity);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName2");
        ItemStack itemStack = entity.getEquipment().getItemInMainHand();
        ItemStack mythicItem = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("MainHand", "FFGGR_Ex"));
        if (world1 != null && world.getName().equals(world1)) {
            if (!mythicItem.isSimilar(itemStack)) {
                removeAttributes(entity);
            }
            if (!this.hasTempDamage(entity) && mythicItem.isSimilar(itemStack) && world.getName().equals(world1)) {
                this.addAttributes(entity);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName2");
        if (!world.getName().equals(world1) && this.hasTempDamage(entity)) {
            removeAttributes(entity);
        }
    }

    private boolean hasTempDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.temp_attack_damage_number")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            for(AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.temp_attack_damage_number")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    private void addAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.temp_attack_damage_number", plugin.getConfig().getDouble("damage_Amount"), AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
