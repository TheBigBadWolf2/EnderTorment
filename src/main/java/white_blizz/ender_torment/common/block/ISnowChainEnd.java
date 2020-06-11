package white_blizz.ender_torment.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public interface ISnowChainEnd extends ISnowChain {
	@Override default boolean isMax(ILightReader world, BlockPos pos, BlockState state) { return true; }
}
