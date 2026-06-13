package net.azisaba.playerbuff.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
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
import org.bukkit.inventory.ItemStack;

public class PlayerBuffTest implements Listener {

    private final PlayerBuff plugin;

    public PlayerBuffTest(PlayerBuff plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LivingEntity entity = event.getPlayer();
        if (this.hasTempHealth(entity)) {
            removeAttributes(entity);
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName");
        ItemStack itemStack = entity.getEquipment().getHelmet();
        ItemStack itemStack1 = entity.getEquipment().getChestplate();
        ItemStack itemStack2 = entity.getEquipment().getLeggings();
        ItemStack itemStack3 = entity.getEquipment().getBoots();
        ItemStack mythicItem = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Head", "HW2023_FF_Head"));
        ItemStack mythicItem1 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Chest", "HW2023_FF_Chest"));
        ItemStack mythicItem2 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Legs", "HW2023_FF_Legs"));
        ItemStack mythicItem3 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Boots", "HW2023_FF_Boots"));
        ItemStack mythicItem22 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Head22", "HW2022_FF_Head"));
        ItemStack mythicItem221 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Chest22", "HW2022_FF_Chest"));
        ItemStack mythicItem222 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Legs22", "HW2022_FF_Legs"));
        ItemStack mythicItem223 = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Boots22", "HW2022_FF_Boots"));
        if (world1 != null && world.getName().equals(world1)) {
            if (entity.getEquipment() == null && world.getName().equals(world1)) {
                removeAttributes(entity);
            }
            if (!mythicItem.isSimilar(itemStack) || mythicItem1.isSimilar(itemStack1) || mythicItem2.isSimilar(itemStack2) || mythicItem3.isSimilar(itemStack3) || !world.getName().equals(world1)) {
                removeAttributes(entity);
            }
            if (!this.hasTempHealth(entity) && world.getName().equals(world1) && mythicItem.isSimilar(itemStack) && !mythicItem221.isSimilar(itemStack1) && !mythicItem222.isSimilar(itemStack2) && !mythicItem223.isSimilar(itemStack3)) {
                this.addAttributes(entity);
            }
            if (!this.hasTempHealth(entity) && world.getName().equals(world1) && mythicItem1.isSimilar(itemStack1) && !mythicItem22.isSimilar(itemStack) && !mythicItem222.isSimilar(itemStack2) && !mythicItem223.isSimilar(itemStack3)) {
                this.addAttributes(entity);
            }
            if (!this.hasTempHealth(entity) && world.getName().equals(world1) && mythicItem2.isSimilar(itemStack2) && !mythicItem22.isSimilar(itemStack) && !mythicItem221.isSimilar(itemStack1) && !mythicItem223.isSimilar(itemStack3)) {
                this.addAttributes(entity);
            }
            if (!this.hasTempHealth(entity) && world.getName().equals(world1) && mythicItem3.isSimilar(itemStack3) && !mythicItem22.isSimilar(itemStack) && !mythicItem221.isSimilar(itemStack1) && !mythicItem222.isSimilar(itemStack2)) {
                this.addAttributes(entity);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName");
        if (!world.getName().equals(world1) && this.hasTempHealth(entity)) {
            removeAttributes(entity);
        }

    }

    private boolean hasTempHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.temp_health_boost_number")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for(AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff.temp_health_boost_number")) {
                    attr.removeModifier(modifier);
                }
            }
        }

    }

    private void addAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff.temp_health_boost_number", plugin.getConfig().getDouble("health_Amount"), AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
