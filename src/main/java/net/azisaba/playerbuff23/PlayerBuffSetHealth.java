package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetHealth {

    private static final String PREFIX_OLD = "PlayerBuff.SetHealth";
    private static final String PREFIX_NEW = "PlayerBuff23.SetHealth";

    public static boolean hasBuffHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return false;
        for (AttributeModifier modifier : attr.getModifiers()) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                return true;
            }
        }
        return false;
    }

    public static void removeHealthAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                attr.removeModifier(modifier);
            }
        }
    }

    public static void addHealthAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        attr.addModifier(new AttributeModifier(PREFIX_NEW, amount, AttributeModifier.Operation.ADD_NUMBER));
    }
}
