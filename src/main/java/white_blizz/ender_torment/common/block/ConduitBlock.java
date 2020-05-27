package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.item.BaseConduitItem;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.tile_entity.ConduitTE;
import white_blizz.ender_torment.common.tile_entity.ETTileEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConduitBlock extends ETBlock {
	public ConduitBlock() {
		super(Block.Properties.create(Material.ROCK)
				.harvestTool(ToolType.PICKAXE));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ConduitTE();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return super.getRaytraceShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return super.getRenderShape(state, worldIn, pos);
	}

	@Override
	public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {

		return state;
	}

	@Override public boolean isVariableOpacity() { return true; }

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		double size = 0.125 / 4;
		double min = 0.5 - size, max = 0.5 + size;
		AxisAlignedBB aabb = new AxisAlignedBB(min, min, min, max, max, max);
		/*return ETTileEntity.get(ConduitTE.class, worldIn, pos)
				.map(te -> te.getLinks()
						.keySet()
						.stream()
						.mapToDouble(ETRegistry::getSortValue)
						.map(sort -> sort * size * 2)
						.mapToObj(sort -> aabb.offset(sort, sort, sort))

						.map(VoxelShapes::create)
						.reduce(VoxelShapes::or)
				)
				.flatMap(Function.identity())
				.orElse(VoxelShapes.fullCube())
		;*/
		return getBoundTypes(worldIn, pos, false)
				.map(type -> type.aabb)
				.map(VoxelShapes::create)
				.reduce(VoxelShapes::or)
				.orElse(VoxelShapes.create(aabb))
				;
		//return VoxelShapes.create(aabb);
	}



	private static class AABBConduitType {
		private static final AxisAlignedBB START;

		static {
			double size = 0.125 / 4;
			double min = 0.5 - size, max = 0.5 + size;
			START = new AxisAlignedBB(min, min, min, max, max, max);
		}

		final ConduitType<?> type;
		final AxisAlignedBB aabb;

		private AABBConduitType(ConduitType<?> type, float off, @Nullable BlockPos pos) {
			this.type = type;
			if (pos != null) aabb = START.offset(off, off, off).offset(pos);
			else aabb = START.offset(off, off, off);
		}
	}

	private Stream<AABBConduitType> getBoundTypes(IBlockReader worldIn, BlockPos pos, boolean toBlock) {
		return ETTileEntity.get(ConduitTE.class, worldIn, pos)
				.map(te -> te.getLinks()
						.keySet()
						.stream()
						.map(type -> new AABBConduitType(type, ETRegistry.getSortValue(type) * (0.0625f), toBlock ? pos : null))
				)
				.orElse(Stream.empty())
		;
	}

	@Override
	public boolean removedByPlayer(
			BlockState state, World world,
			BlockPos pos, PlayerEntity player,
			boolean willHarvest, IFluidState fluid) {
		RayTraceResult result = trace(state, world, pos, player);
		if (result.hitInfo instanceof ConduitType &&
			ETTileEntity.get(ConduitTE.class, world, pos)
					.map(te -> te.removeType((ConduitType<?>) result.hitInfo))
					.orElse(false)) {
			if (willHarvest)
			Block.spawnAsEntity(world, pos, ETItems.CONDUIT.get().fromType((ConduitType<?>) result.hitInfo));
			return false;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	private BlockRayTraceResult trace(BlockState state, World world, BlockPos pos, Entity entity) {
		RayTraceContext.BlockMode mode = RayTraceContext.BlockMode.OUTLINE;
		Vec3d start = entity.getEyePosition(1F);
		Vec3d end = start.add(entity.getLook(1F).scale(5));

		BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(
				start, end, mode,
				RayTraceContext.FluidMode.NONE,
				entity
		));

		getBoundTypes(world, pos, true)
				.filter(type -> type.aabb.contains(result.getHitVec()))
				.map(type -> type.type)
				.findFirst()
				.ifPresent(type -> result.hitInfo = type);

		return result;
	}

	/*@Nullable
	@Override
	public RayTraceResult getRayTraceResult(
			BlockState state, World world,
			BlockPos pos, Vec3d start, Vec3d end,
			RayTraceResult original) {
		if (original.getType() == RayTraceResult.Type.BLOCK) {
			ETTileEntity.get(ConduitTE.class, world, pos)

					.ifPresent(te -> {

					});
		}

		return original;
	}*/
}
