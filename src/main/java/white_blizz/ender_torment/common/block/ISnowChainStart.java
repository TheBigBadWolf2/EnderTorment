package white_blizz.ender_torment.common.block;

import net.minecraft.block.BlockState;

public interface ISnowChainStart extends ISnowChain {
	BlockState next(BlockState current);
}
