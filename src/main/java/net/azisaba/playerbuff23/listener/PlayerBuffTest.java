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

import java.util.Objects;

public class PlayerBuffTest implements Listener {

    private static final String TEMP_HEALTH_MODIFIER_OLD = "PlayerBuff.temp_health_boost_number";
    private static final String TEMP_HEALTH_MODIFIER_NEW = "PlayerBuff23.temp_health_boost_number";

    private final PlayerBuff23 plugin;

    public PlayerBuffTest(PlayerBuff23 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LivingEntity entity = event.getPlayer();
        if (hasTempHealth(entity)) {
            removeAttributes(entity);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String worldName = plugin.getConfig().getString("worldName");
        if (worldName == null || !world.getName().equals(worldName)) return;

        ItemStack helmet = Objects.requireNonNull(entity.getEquipment()).getHelmet();
        ItemStack chestplate = entity.getEquipment().getChestplate();
        ItemStack leggings = entity.getEquipment().getLeggings();
        ItemStack boots = entity.getEquipment().getBoots();

        // HW2023 items
        ItemStack hw23Helmet = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Head", "HW2023_FF_Head"));
        ItemStack hw23Chest = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Chest", "HW2023_FF_Chest"));
        ItemStack hw23Legs = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Legs", "HW2023_FF_Legs"));
        ItemStack hw23Boots = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Boots", "HW2023_FF_Boots"));

        // HW2022 items
        ItemStack hw22Helmet = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Head22", "HW2022_FF_Head"));
        ItemStack hw22Chest = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Chest22", "HW2022_FF_Chest"));
        ItemStack hw22Legs = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Legs22", "HW2022_FF_Legs"));
        ItemStack hw22Boots = MythicMobs.inst().getItemManager().getItemStack(plugin.getConfig().getString("Boots22", "HW2022_FF_Boots"));

        // Check if any equipped piece matches HW2022 (individual piece = get buff)
        boolean hasHw22Piece = hw22Helmet.isSimilar(helmet) || hw22Chest.isSimilar(chestplate)
            || hw22Legs.isSimilar(leggings) || hw22Boots.isSimilar(boots);

        // Check HW2023 mixed set: one piece HW2023, other pieces NOT HW2022
        boolean hasHw23Set = (hw23Helmet.isSimilar(helmet) && !hw22Chest.isSimilar(chestplate)
                && !hw22Legs.isSimilar(leggings) && !hw22Boots.isSimilar(boots))
            || (hw23Chest.isSimilar(chestplate) && !hw22Helmet.isSimilar(helmet)
                && !hw22Legs.isSimilar(leggings) && !hw22Boots.isSimilar(boots))
            || (hw23Legs.isSimilar(leggings) && !hw22Helmet.isSimilar(helmet)
                && !hw22Chest.isSimilar(chestplate) && !hw22Boots.isSimilar(boots))
            || (hw23Boots.isSimilar(boots) && !hw22Helmet.isSimilar(helmet)
                && !hw22Chest.isSimilar(chestplate) && !hw22Legs.isSimilar(leggings));

        if (!hasTempHealth(entity)) {
            if (hasHw22Piece || hasHw23Set) {
                addAttributes(entity);
            }
        } else if (!hasHw22Piece && !hasHw23Set) {
            removeAttributes(entity);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LivingEntity entity = event.getPlayer();
        World world = entity.getWorld();
        String world1 = plugin.getConfig().getString("worldName");
        if (!world.getName().equals(world1) && hasTempHealth(entity)) {
            removeAttributes(entity);
        }
    }

    private static boolean hasTempHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return false;
        for (AttributeModifier modifier : attr.getModifiers()) {
            String name = modifier.getName();
            if (name.equals(TEMP_HEALTH_MODIFIER_OLD) || name.equals(TEMP_HEALTH_MODIFIER_NEW)) {
                return true;
            }
        }
        return false;
    }

    private static void removeAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        for (AttributeModifier modifier : attr.getModifiers()) {
            String name = modifier.getName();
            if (name.equals(TEMP_HEALTH_MODIFIER_OLD) || name.equals(TEMP_HEALTH_MODIFIER_NEW)) {
                attr.removeModifier(modifier);
            }
        }
    }

    private void addAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        attr.addModifier(new AttributeModifier(TEMP_HEALTH_MODIFIER_NEW, plugin.getConfig().getDouble("health_Amount"), AttributeModifier.Operation.ADD_NUMBER));
    }
}
