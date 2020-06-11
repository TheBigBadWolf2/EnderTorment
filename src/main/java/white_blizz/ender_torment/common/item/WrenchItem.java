package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WrenchItem extends ETItem {
	private static final ToolType WRENCH = ToolType.get("wrench");

	public WrenchItem() {
		super(new Item.Properties().group(ETItems.ENDER_MISC)
				.addToolType(WRENCH, 1));
	}

	private boolean isWrenchableBlock(BlockState state) { return state.isToolEffective(WRENCH); }
	private boolean isWrenchableBlock(IWorldReader world, BlockPos pos) { return isWrenchableBlock(world.getBlockState(pos)); }

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
		return isWrenchableBlock(world, pos);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return !isWrenchableBlock(state) && !player.isCreative();
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState oldState = world.getBlockState(pos);
		BlockState newState = oldState.rotate(world, pos, Rotation.CLOCKWISE_90);
		if (oldState != newState && world.setBlockState(pos, newState))
			return ActionResultType.SUCCESS;
		return ActionResultType.PASS;
	}
}
