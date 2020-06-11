package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class NonStartSnowBlock extends ETBlock implements ISnowChain {
	public NonStartSnowBlock(Block mimics) {
		super(Block.Properties.from(mimics));
	}

	@Override
	public int getLevel(ILightReader world, BlockPos pos, BlockState state) {
		return MAX;
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ISnowChain;
	}
}
