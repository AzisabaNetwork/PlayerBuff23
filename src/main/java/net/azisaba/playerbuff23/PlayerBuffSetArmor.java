package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetArmor {

    private static final String PREFIX_OLD = "PlayerBuff.SetArmor";
    private static final String PREFIX_NEW = "PlayerBuff23.SetArmor";

    public static boolean hasBuffArmor(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr == null) return false;
        for (AttributeModifier modifier : attr.getModifiers()) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                return true;
            }
        }
        return false;
    }

    public static void removeArmorAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr == null) return;
        for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                attr.removeModifier(modifier);
            }
        }
    }

    public static void addArmorAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR);
        if (attr == null) return;
        attr.addModifier(new AttributeModifier(PREFIX_NEW, amount, AttributeModifier.Operation.ADD_NUMBER));
    }
}
