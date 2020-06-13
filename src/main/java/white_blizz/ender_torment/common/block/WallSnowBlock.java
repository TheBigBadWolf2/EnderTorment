package white_blizz.ender_torment.common.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import white_blizz.ender_torment.common.state.DirectionalProperty;
import white_blizz.ender_torment.common.state.LayeredAxisProperty;
import white_blizz.ender_torment.common.tile_entity.WallSnowTE;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings({"deprecation"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WallSnowBlock extends ETBlock {
	private static final int MAX = 8;

	private static final Map<Direction, VoxelShape[]> SHAPE_MAP;

	private static IntStream stream(Vec3i vec) { return IntStream.of(vec.getY(), vec.getY(), vec.getZ()); }
	private static DoubleStream stream(Vec3d vec) { return DoubleStream.of(vec.x, vec.y, vec.z); }

	private static int getOr(int[] array, int index) {
		int l = array.length;
		if (0 <= index && index < l) return array[index];
		return 0;
	}
	private static double getOr(double[] array, int index) {
		int l = array.length;
		if (0 <= index && index < l) return array[index];
		return 0;
	}

	private static Vec3i toVec(IntStream stream) {
		int[] array = stream.limit(3).toArray();
		return new Vec3i(
				getOr(array, 0),
				getOr(array, 1),
				getOr(array, 2)
		);
	}

	private static Vec3d toVec(DoubleStream stream) {
		double[] array = stream.limit(3).toArray();
		return new Vec3d(
				getOr(array, 0),
				getOr(array, 1),
				getOr(array, 2)
		);
	}

	private static Vec3d map(Vec3i vec, IntToDoubleFunction mapper) {
		return toVec(stream(vec).mapToDouble(mapper));
	}

	private static VoxelShape map(Direction dir, IntToDoubleFunction a, IntToDoubleFunction b) {
		Vec3i vec = dir.getDirectionVec();
		return VoxelShapes.create(
				new AxisAlignedBB(
						map(vec, a),
						map(vec, b)
				)
		);
	}

	static {
		EnumMap<Direction, VoxelShape[]> shapeMap = new EnumMap<>(Direction.class);

		for (Direction dir : Direction.values()) {
			VoxelShape[] shapes = new VoxelShape[MAX + 1];
			shapes[0] = VoxelShapes.empty();

			for (int l = 1; l <= MAX; l++) {
				double d = (double) l / (double) MAX;
				shapes[l] = map(dir, i -> {
					if (i < 0) return 1D-d;
					return 0D;
				}, i -> {
					if (i > 0) return d;
					return 1D;
				});
			}

			shapeMap.put(dir, shapes);
		}

		SHAPE_MAP = ImmutableMap.copyOf(shapeMap);
	}

	public static DirectionalProperty DIRECTIONS = new DirectionalProperty("directions", true);

	public WallSnowBlock() {
		super(Properties.create(Material.SNOW).notSolid());
		setDefaultState(DIRECTIONS.with(getDefaultState(), null));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(DIRECTIONS);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}


	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable @Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new WallSnowTE();
	}

	private static final List<Block> BLACKLIST = ImmutableList.of(
			Blocks.ICE, Blocks.PACKED_ICE, Blocks.BARRIER
	), WHITELIST = ImmutableList.of(
			Blocks.HONEY_BLOCK, Blocks.SOUL_SAND
	);

	/*@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return getDefaultState();
	}*/

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == this && newState.getBlock() == this) {
			Direction dir = DIRECTIONS.get(newState);
			if (dir != null) {
				WallSnowTE.get(worldIn, pos).ifPresent(te -> te.addLayer(dir));
				worldIn.setBlockState(pos, DIRECTIONS.with(newState, null));
			}
		} else super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() == this;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(DIRECTIONS, context.getFace().getIndex());
	}
}
