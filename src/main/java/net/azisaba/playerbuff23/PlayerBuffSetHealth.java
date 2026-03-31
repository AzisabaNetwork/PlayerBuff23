package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetHealth {

    public static boolean hasBuffHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.SetHealth")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeHealthAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            for(AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
                if (modifier.getName().equals("PlayerBuff23.SetHealth")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    public static void addHealthAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.SetHealth", amount, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
