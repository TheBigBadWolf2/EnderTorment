package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class ETEnchantment extends Enchantment implements IETEnchantment {
	protected ETEnchantment(Rarity rarity, EnchantmentType type, EquipmentSlotType... slots) {
		super(rarity, type, slots);
	}
}
