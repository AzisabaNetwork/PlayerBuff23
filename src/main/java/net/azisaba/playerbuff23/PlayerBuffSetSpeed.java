package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetSpeed {

    public static boolean hasBuffSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.SetSpeed")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeSpeedAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            for(AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
                if (modifier.getName().equals("PlayerBuff23.SetSpeed")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    public static void addSpeedAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.SetSpeed", amount, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
