package white_blizz.ender_torment.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;

public interface ISnowChainEnd extends ISnowChain {
	@Override default boolean isMax(ILightReader world, BlockPos pos, BlockState state) { return true; }

	@Override default boolean testNext(BlockState state, IWorldReader worldIn, BlockPos pos) { return false; }
	@Override default BlockState next(BlockState current) { return current; }
}
