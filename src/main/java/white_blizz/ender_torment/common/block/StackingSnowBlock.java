package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackingSnowBlock extends ETBlock implements ISnowChainStart {
	public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX);
	protected static final VoxelShape[] SHAPES;

	static {
		SHAPES = new VoxelShape[MAX + 1];
		SHAPES[0] = VoxelShapes.empty();
		for (int i = 1; i <= MAX; i++) {
			SHAPES[i] = makeCuboidShape(
					0.0D, 0.0D, 0.0D,
					16.0D, (i * 16F) / MAX, 16.0D
			);
		}
	}

	public StackingSnowBlock() {
		super(Block.Properties.from(Blocks.SNOW));
		this.setDefaultState(this.stateContainer.getBaseState().with(LAYERS, 1));
	}

	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		if (type == PathType.LAND) return state.get(LAYERS) <= MAX / 2;
		return false;
	}

	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.get(LAYERS)];
	}

	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES[state.get(LAYERS) - 1];
	}

	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = context.getWorld().getBlockState(context.getPos());
		if (blockstate.getBlock() == this) {
			int i = blockstate.get(LAYERS);
			return blockstate.with(LAYERS, Math.min(MAX, i + 1));
		} else {
			return super.getStateForPlacement(context);
		}
	}

	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		int i = state.get(LAYERS);
		if (useContext.getItem().getItem() == this.asItem() && i < 8) {
			if (useContext.replacingClickedOnBlock()) {
				return useContext.getFace() == Direction.UP;
			} else {
				return true;
			}
		} else {
			return i == 1;
		}
	}

	public boolean isTransparent(BlockState state) {
		return true;
	}

	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockstate = worldIn.getBlockState(pos.down());
		Block block = blockstate.getBlock();
		if (block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER) {
			if (block != Blocks.HONEY_BLOCK && block != Blocks.SOUL_SAND) {
				if (!Block.doesSideFillSquare(blockstate.getCollisionShape(worldIn, pos.down()), Direction.UP) && (block != this || blockstate.get(LAYERS) != 8)) {
					return false;
				}
			}
			/*BlockPos down = pos;
			BlockState state1;
			while ((state1 = worldIn.getBlockState(down = down.down())).getBlock() instanceof ISnowChain) {
				if (state1.getBlock() instanceof ISnowChainEnd) return false;
			}*/
			return testNext(state, worldIn, pos);
		} else {
			return false;
		}
	}


	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return post(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LAYERS);
	}

	@Override
	public int getLevel(ILightReader world, BlockPos pos, BlockState state) {
		return state.get(LAYERS);
	}

	@Override
	public boolean isMax(ILightReader world, BlockPos pos, BlockState state) {
		BlockPos up = pos.up();
		return world.getBlockState(up).getBlock() instanceof ISnowChain || world.getBlockState(up).isSolidSide(world, up, Direction.DOWN);
	}

	@Override
	public boolean testNext(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos down = pos.down();
		BlockState next = worldIn.getBlockState(down);
		Block block = next.getBlock();
		if (block == this) {
			return testNext(next, worldIn, down);
		} else if (next(state) == next) {
			if (block instanceof ISnowChainMiddle) {
				return ((ISnowChainMiddle) block).testNext(next, worldIn, down);
			} else return !(block instanceof ISnowChainEnd);
		}
		return true;
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ISnowChain;
	}

	@Override
	public BlockState next(BlockState current) {
		if (current.get(LAYERS) == MAX) return ETBlocks.SNOW_BLOCK.get().getDefaultState();
		return current;
	}
}
