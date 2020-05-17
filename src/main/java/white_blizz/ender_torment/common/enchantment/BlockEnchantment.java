package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class BlockEnchantment extends ETEnchantment {
	protected BlockEnchantment(Rarity rarity, EnchantmentType type) {
		super(rarity, type, EquipmentSlotType.MAINHAND);
	}
}
