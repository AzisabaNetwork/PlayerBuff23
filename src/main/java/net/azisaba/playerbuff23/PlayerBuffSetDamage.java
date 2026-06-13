package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetDamage {

    private static final String PREFIX_OLD = "PlayerBuff.SetDamage";
    private static final String PREFIX_NEW = "PlayerBuff23.SetDamage";

    public static boolean hasBuffDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return false;
        for (AttributeModifier modifier : attr.getModifiers()) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                return true;
            }
        }
        return false;
    }

    public static void removeDamageAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;
        for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
            String name = modifier.getName();
            if (name.equals(PREFIX_OLD) || name.equals(PREFIX_NEW)) {
                attr.removeModifier(modifier);
            }
        }
    }

    public static void addDamageAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;
        attr.addModifier(new AttributeModifier(PREFIX_NEW, amount, AttributeModifier.Operation.ADD_NUMBER));
    }
}
