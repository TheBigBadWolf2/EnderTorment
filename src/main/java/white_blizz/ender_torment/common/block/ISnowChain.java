package white_blizz.ender_torment.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;

public interface ISnowChain extends IETBlock {
	int MAX = 8;
	int getLevel(ILightReader world, BlockPos pos, BlockState state);
	boolean isMax(ILightReader world, BlockPos pos, BlockState state);

	default BlockState post(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.UP) {
			if (stateIn == facingState) {
				if (this instanceof ISnowChainStart) return ((ISnowChainStart) this).next(stateIn);
				else if (this instanceof ISnowChainMiddle) return ((ISnowChainMiddle) this).next(stateIn);
			}
		}
		return stateIn;
	}
}
