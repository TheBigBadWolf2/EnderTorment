package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.BlockItem;
import white_blizz.ender_torment.common.block.EnderFluxCollectorBlock;
import white_blizz.ender_torment.common.block.IEnderFluxBlock;
import white_blizz.ender_torment.common.item.IEnderFluxItem;
import white_blizz.ender_torment.utils.Ref;

public class ETEnchantmentType {
	public static final EnchantmentType ENDER_FLUX_ITEMS =
			EnchantmentType.create(
					Ref.locStr("ender_flux_item"),
					item -> {
						if (item instanceof IEnderFluxItem) return true;
						if (item instanceof BlockItem) {
							return ((BlockItem) item).getBlock() instanceof IEnderFluxBlock;
						}
						return false;
					}
			);
	public static final EnchantmentType ENDER_FLUX_GEN_ITEMS =
			EnchantmentType.create(
					Ref.locStr("ender_flux_gen_item"),
					item -> {
						if (item instanceof IEnderFluxItem) return true;
						if (item instanceof BlockItem) {
							return ((BlockItem) item).getBlock() instanceof EnderFluxCollectorBlock;
						}
						return false;
					}
			);
}
