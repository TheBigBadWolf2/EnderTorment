package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MiddleSnowBlock extends NonStartSnowBlock implements ISnowChainMiddle {
	private final Supplier<BlockState> next;

	public static Supplier<MiddleSnowBlock> withState(Block mimics, Supplier<BlockState> next) {
		return () -> new MiddleSnowBlock(mimics, next);
	}
	public static Supplier<MiddleSnowBlock> withBlock(Block mimics, Supplier<? extends Block> next) {
		return () -> new MiddleSnowBlock(mimics, () -> next.get().getDefaultState());
	}

	private MiddleSnowBlock(Block mimics, Supplier<BlockState> next) {
		super(mimics);
		this.next = next;
	}


	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return post(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public BlockState next(BlockState current) {
		return next.get();
	}
}
