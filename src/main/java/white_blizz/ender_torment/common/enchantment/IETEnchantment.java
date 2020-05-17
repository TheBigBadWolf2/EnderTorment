package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.Enchantment;

public interface IETEnchantment {
	default Enchantment getEnchantment() { return (Enchantment) this; }

	default boolean isBinary() {
		return getEnchantment().getMinLevel() == getEnchantment().getMaxLevel();
	}
}
