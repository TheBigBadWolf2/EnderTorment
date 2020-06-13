package white_blizz.ender_torment.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public interface ISnowChain extends IETBlock {
	int MAX = 8;
	int getLevel(ILightReader world, BlockPos pos, BlockState state);

	BlockState next(BlockState current);

	boolean isMax(ILightReader world, BlockPos pos, BlockState state);

	default BlockState post(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.UP) {
			if (stateIn == facingState) {
				return this.next(stateIn);
			}
		}
		return stateIn;
	}

	boolean testNext(BlockState state, IWorldReader worldIn, BlockPos pos);
}
