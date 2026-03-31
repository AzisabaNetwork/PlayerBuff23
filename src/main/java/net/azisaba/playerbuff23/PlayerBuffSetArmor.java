package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

public class PlayerBuffSetArmor {

    public static boolean hasBuffArmor(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                return modifier.getName().equals("PlayerBuff23.SetArmor");
            }
        }
        return false;
    }

    public static void removeArmorAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr != null) {
            attr.getModifiers().removeIf(modifier -> modifier.getName().equals("PlayerBuff23.SetArmor"));
        }
    }

    public static void addArmorAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.SetArmor", amount, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
