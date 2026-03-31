package net.azisaba.playerbuff23;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class PlayerBuffSetArmorToughness {

    public static boolean hasBuffArmorToughness(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (attr != null) {
            for (AttributeModifier modifier : attr.getModifiers()) {
                if (modifier.getName().equals("PlayerBuff23.SetArmor_Toughness")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeArmorToughnessAttributes(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (attr != null) {
            for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
                if (modifier.getName().equals("PlayerBuff23.SetArmor_Toughness")) {
                    attr.removeModifier(modifier);
                }
            }
        }
    }

    public static void addArmorToughnessAttributes(LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (attr != null) {
            attr.addModifier(new AttributeModifier("PlayerBuff23.SetArmor_Toughness", amount, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
