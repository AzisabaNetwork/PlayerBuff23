package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

public class PlayerBuffSetDamage {

    public static boolean hasBuffDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.SetDamage")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeDamageAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            for(AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.SetDamage")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    public static void addDamageAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.SetDamage", amount, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
