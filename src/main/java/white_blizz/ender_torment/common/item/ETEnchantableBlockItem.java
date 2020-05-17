package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import white_blizz.ender_torment.common.enchantment.CapabilityEnchantableBlock;
import white_blizz.ender_torment.utils.ETUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ETEnchantableBlockItem extends ETBlockItem {
	public ETEnchantableBlockItem(Supplier<? extends Block> blockIn, Properties builder) {
		super(blockIn, builder);
	}

	@Override
	protected boolean onBlockPlaced(
			BlockPos pos, World worldIn,
			@Nullable PlayerEntity player,
			ItemStack stack, BlockState state) {
		boolean flag = super.onBlockPlaced(pos, worldIn, player, stack, state);

		ETUtils.getCap(worldIn, pos, CapabilityEnchantableBlock.ENCHANTABLE_BLOCK)
				.ifPresent(enchantable -> enchantable.deserializeNBT(stack.getEnchantmentTagList()));

		return flag;
	}

	@Override public int getItemEnchantability() { return 1; }
}
