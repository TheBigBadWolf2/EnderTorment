package white_blizz.ender_torment.common.enchantment;

public class DecayResistEnchantment extends BlockEnchantment {
	public DecayResistEnchantment() {
		super(Rarity.RARE, ETEnchantmentType.ENDER_FLUX_ITEMS);
	}

	@Override public int getMaxLevel() { return 10; }
}
