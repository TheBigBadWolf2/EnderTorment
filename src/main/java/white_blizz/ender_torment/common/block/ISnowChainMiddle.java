package white_blizz.ender_torment.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;

public interface ISnowChainMiddle extends ISnowChain {
	@Override default boolean isMax(ILightReader world, BlockPos pos, BlockState state) { return true; }

	@Override
	default boolean testNext(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos down = pos.down();
		BlockState next = worldIn.getBlockState(down);
		Block block = next.getBlock();
		if (next(state) == next) {
			if (block instanceof ISnowChain) {
				return ((ISnowChain) block).testNext(next, worldIn, down);
			}
		}
		return true;
	}
}
