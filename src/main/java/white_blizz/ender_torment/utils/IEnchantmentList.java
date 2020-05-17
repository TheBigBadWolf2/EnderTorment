package white_blizz.ender_torment.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;
import java.util.function.Supplier;

public interface IEnchantmentList {
	Map<Enchantment, Integer> getEnchantments();

	int count();

	EnchantmentData getEnchant(int slot);
	void remove(Enchantment enchant);
	void add(Enchantment enchant, int level);
	void add(CompoundNBT tag);

	default int getLevel(Supplier<? extends Enchantment> enchant) {
		return getLevel(enchant.get());
	}

	default int getLevel(Enchantment enchant) {
		return getEnchantments().getOrDefault(enchant, 0);
	}
}
