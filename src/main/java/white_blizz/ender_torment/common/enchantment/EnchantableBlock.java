package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Objects;

public class EnchantableBlock implements IEnchantableBlock {
	protected static CompoundNBT toTag(Enchantment enchant, int lvl) {
		CompoundNBT tag = new CompoundNBT();
		tag.putString("id", String.valueOf(ForgeRegistries.ENCHANTMENTS.getKey(enchant)));
		tag.putShort("lvl", (short)lvl);
		return tag;
	}
	protected static ListNBT toNBT(Map<Enchantment, Integer> enchMap) {
		ListNBT list = new ListNBT();

		for(Map.Entry<Enchantment, Integer> entry : enchMap.entrySet()) {
			Enchantment enchantment = entry.getKey();
			if (enchantment != null) {
				int i = entry.getValue();
				list.add(toTag(enchantment, i));
			}
		}
		return list;
	}

	protected static EnchantmentData toData(CompoundNBT tag) {
		return new EnchantmentData(
				Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(tag.getString("id")))),
				tag.getInt("lvl")
		);
	}

	protected ListNBT enchants;

	public EnchantableBlock(ListNBT enchants) {
		this.enchants = enchants;
	}

	@Override public ListNBT serializeNBT() { return enchants.copy(); }
	@Override public void deserializeNBT(ListNBT list) { enchants = list.copy(); }

	@Override
	public Map<Enchantment, Integer> getEnchantments() {
		return EnchantmentHelper.deserializeEnchantments(enchants);
	}

	@Override public int count() { return enchants.size(); }

	@Override
	public EnchantmentData getEnchant(int slot) {
		return toData(enchants.getCompound(slot));
	}

	@Override
	public void remove(Enchantment enchant) {
		Map<Enchantment, Integer> map = getEnchantments();
		map.remove(enchant);
		enchants = toNBT(map);
	}

	@Override public void add(Enchantment enchant, int level) { add(toTag(enchant, level)); }
	@Override public void add(CompoundNBT tag) { enchants.add(tag); }


}
