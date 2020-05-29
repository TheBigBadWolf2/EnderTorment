package white_blizz.ender_torment.common.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.ETTags;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.conduit.Link;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.tile_entity.ConduitTE;
import white_blizz.ender_torment.common.tile_entity.ETTileEntity;
import white_blizz.ender_torment.utils.IndexedValue;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConduitBlock extends ETBlock {
	public ConduitBlock() {
		super(Block.Properties.create(Material.ROCK)
				.harvestTool(ToolType.get("wrench")));
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
		return getRawShape(worldIn, pos);
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getRawShape(worldIn, pos);
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

	@Override //Todo: cashe the shapes.
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Entity entity = context.getEntity();
		if (entity != null && (context.func_225581_b_() || (entity instanceof LivingEntity && isWrench(((LivingEntity) entity).getHeldItemMainhand())))) {
			BlockRayTraceResult result = trace(worldIn, pos, entity);
			if (result.hitInfo instanceof ConduitTypeBB) {
				ConduitTypeBB info = (ConduitTypeBB) result.hitInfo;
				AxisAlignedBB bb;
				if (result.subHit < 0) bb = info.mainA;
				else bb = info.connectionsA.get(result.subHit).aabb;
				return VoxelShapes.create(bb);
			}
		}
		return getRawShape(worldIn, pos);
		//return VoxelShapes.create(aabb);
	}

	private VoxelShape getRawShape(IBlockReader world, BlockPos pos) {
		double size = 0.0625 / 2;
		double min = 0.5 - size, max = 0.5 + size;
		AxisAlignedBB aabb = new AxisAlignedBB(min, min, min, max, max, max);
		return getBoundTypes(world, pos)
				.map(type -> type.connectionsA
						.stream()
						.map(dbb -> dbb.aabb)
						.map(VoxelShapes::create)
						.reduce(VoxelShapes.create(type.mainA), VoxelShapes::or)
				)
				.reduce(VoxelShapes::or)
				.orElse(VoxelShapes.create(aabb))
				;
	}

	private static class DirBB {
		private final Direction dir;
		private final AxisAlignedBB aabb;

		private DirBB(Direction dir, AxisAlignedBB aabb) {
			this.dir = dir;
			this.aabb = aabb;
		}
	}

	private static class ConduitTypeBB {
		private static final AxisAlignedBB START;
		private static final double scale = 0.0625;
		static {
			double size = 0.125 / 4;
			double min = 0.5 - size, max = 0.5 + size;
			START = new AxisAlignedBB(min, min, min, max, max, max);
		}

		private static AxisAlignedBB offset(AxisAlignedBB aabb, @Nullable BlockPos pos) {
			if (pos != null) return aabb.offset(pos);
			return aabb;
		}

		final ConduitType<?> type;
		final AxisAlignedBB mainA, mainB;
		final List<DirBB> connectionsA, connectionsB;

		private ConduitTypeBB(Link<?> link, BlockPos pos) {
			this.type = link.getType();
			double off = ETRegistry.getSortValue(link.getType(), scale);
			mainA = START.offset(off, off, off);
			mainB = mainA.offset(pos);
			connectionsA = link.getConnections()
					.keySet()
					.stream()
					.map(dir -> {
						Vec3d vec = new Vec3d(dir.getDirectionVec());
						Vec3d unit = vec.scale(scale);
						AxisAlignedBB bb = mainA.offset(unit);
						unit = unit.scale(1.5);
						double ex;
						switch (dir.getAxisDirection()) {
							case POSITIVE: ex = 0.5D - off; break;
							case NEGATIVE: ex = 0.5D + off; break;
							default: ex = 0.5; break;
						}
						return new DirBB(dir, bb.expand(vec.scale(ex)).contract(unit.x, unit.y, unit.z));
					})
					.collect(ImmutableList.toImmutableList())
					;
			connectionsB = connectionsA.stream()
					.map(other -> new DirBB(other.dir, other.aabb.offset(pos)))
					.collect(ImmutableList.toImmutableList());
		}

		public Stream<AxisAlignedBB> streamA() { return Stream.concat(Stream.of(mainA), connectionsA.stream().map(dbb -> dbb.aabb)); }
		public Stream<AxisAlignedBB> streamB() { return Stream.concat(Stream.of(mainB), connectionsB.stream().map(dbb -> dbb.aabb)); }
	}

	private Stream<ConduitTypeBB> getBoundTypes(IBlockReader worldIn, BlockPos pos) {
		return ETTileEntity.get(ConduitTE.class, worldIn, pos)
				.map(te -> te.getLinks()
						.values()
						.stream()
						.map(link -> new ConduitTypeBB(link, pos))
				)
				.orElse(Stream.empty())
		;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		BlockRayTraceResult result = trace(world, pos, player);
		if (result.hitInfo instanceof ConduitTypeBB) {
			ConduitTypeBB info = (ConduitTypeBB) result.hitInfo;
			return ETItems.CONDUIT.get().fromType(info.type);
		}
		ItemStack stack = new ItemStack(Items.EGG);
		stack.setDisplayName(new StringTextComponent("Here, have an error egg."));
		return stack; //Why? Because I said so.
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!isWrench(player.getHeldItem(handIn))) return ActionResultType.PASS;
		return ETTileEntity.get(ConduitTE.class, world, pos).map(te -> {
			BlockRayTraceResult trace = trace(world, pos, player);
			if (trace.hitInfo instanceof ConduitTypeBB) {
				ConduitTypeBB info = (ConduitTypeBB) trace.hitInfo;
				if (trace.subHit < 0) {
					if (player.isSneaking()) {
						//player.sendStatusMessage(new StringTextComponent("!"), true);
						if (te.removeType(info.type)) {
							Block.spawnAsEntity(world, pos, ETItems.CONDUIT.get().fromType(info.type));
							if (te.getLinks().isEmpty()) world.destroyBlock(pos, false);
							return ActionResultType.SUCCESS;
						}
					}
					if (te.setConnection(trace.getFace(), info.type, true)) {
						return ActionResultType.SUCCESS;
					}
				} else {
					Direction dir = info.connectionsA.get(trace.subHit).dir;
					if (player.isSneaking()) {
						return ActionResultType.FAIL;
					}
					if (te.setConnection(dir, info.type, false)) {
						return ActionResultType.SUCCESS;
					}

				}
			} else {
				if (!world.isRemote()) {
					//ChickenEntity chicken = new ChickenEntity(EntityType.CHICKEN, world);
					ItemEntity chicken = new ItemEntity(world, 0, 0, 0, new ItemStack(Items.COOKED_CHICKEN));
					chicken.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					chicken.setCustomName(
							new StringTextComponent("Error: ")
									.applyTextStyle(TextFormatting.RED)
									.appendSibling(new StringTextComponent("Chicken Found")
											.applyTextStyle(style -> {
												style.setColor(TextFormatting.WHITE);
											}))
					);
					chicken.setCustomNameVisible(true);
					world.addEntity(chicken);
				}
			}
			return ActionResultType.FAIL;
		}).orElse(ActionResultType.FAIL);
	}

	@Override
	public boolean removedByPlayer(
			BlockState state, World world,
			BlockPos pos, PlayerEntity player,
			boolean willHarvest, IFluidState fluid) {
		RayTraceResult result = trace(world, pos, player);
		if (result.hitInfo instanceof ConduitTypeBB &&
			ETTileEntity.get(ConduitTE.class, world, pos)
					.map(te -> te.removeType(((ConduitTypeBB) result.hitInfo).type))
					.orElse(false)
		) {
			if (willHarvest)
			Block.spawnAsEntity(world, pos, ETItems.CONDUIT.get().fromType(((ConduitTypeBB) result.hitInfo).type));
		}
		if (ETTileEntity.get(ConduitTE.class, world, pos)
				.map(ConduitTE::getLinks)
				.map(Map::isEmpty)
				.orElse(true))
			return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
		return false;
	}

	private BlockRayTraceResult trace(IBlockReader world, BlockPos pos, Entity entity) {
		RayTraceContext.BlockMode mode = RayTraceContext.BlockMode.COLLIDER;
		Vec3d start = entity.getEyePosition(1F);
		Vec3d end = start.add(entity.getLook(1F).scale(5));

		BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(
				start, end, mode,
				RayTraceContext.FluidMode.NONE,
				entity
		));

		class Hits {
			final ConduitTypeBB type;
			final List<Optional<Vec3d>> hits;

			Hits(ConduitTypeBB type, List<Optional<Vec3d>> hits) {
				this.type = type;
				this.hits = hits;
			}

			double dist() {
				return hits.stream()
						.filter(Optional::isPresent)
						.map(Optional::get)
						.mapToDouble(vec -> vec.distanceTo(start))
						.min()
						.orElse(Double.POSITIVE_INFINITY);
			}
		}

		/*.filter(type -> type.stream()
						.anyMatch(aabb -> aabb.grow(0.01D).contains(result.getHitVec())
				))*/
		//.map(type -> type.type)
		getBoundTypes(world, pos)
				.map(type -> new Hits(type, type.streamB()
						.map(aabb -> aabb.rayTrace(start, end))
						.collect(Collectors.toList())
				))
				.filter(hits -> hits.hits.stream().anyMatch(Optional::isPresent))
				.min(Comparator.comparingDouble(Hits::dist))
				.ifPresent(hit -> {
					result.hitInfo = hit.type;
					result.subHit = Streams.zip(
							Stream.iterate(-1, i -> i + 1),
							hit.hits.stream(),
							IndexedValue::new
					)
							.filter(i -> i.value.isPresent())
							.map(i -> new IndexedValue<>(i.index, i.value.get()))
							.min(Comparator.comparingDouble(a -> a.value.distanceTo(start)))
							.map(i -> i.index)
							.orElse(-1);
				});

		return result;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {

	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		//te.checkNeighbor(fromPos);
		if (!world.isRemote() && world.getBlockState(fromPos).getBlock() != this)
			ETTileEntity.get(ConduitTE.class, world, pos).ifPresent(ConduitTE::checkNeighbors);
	}


	private boolean isWrench(ItemStack stack) {
		return !stack.isEmpty() && (stack.getItem().isIn(ETTags.Items.WRENCH) || stack.getHarvestLevel(ToolType.get("wrench"), null, null) >= 1);
	}



	@Nullable
	@Override
	public RayTraceResult getRayTraceResult(
			BlockState state, World world,
			BlockPos pos, Vec3d start, Vec3d end,
			RayTraceResult original) {
		if (original.getType() == RayTraceResult.Type.BLOCK) {
			class Hits {
				final ConduitTypeBB type;
				final List<Optional<Vec3d>> hits;

				Hits(ConduitTypeBB type, List<Optional<Vec3d>> hits) {
					this.type = type;
					this.hits = hits;
				}

				double dist() {
					return hits.stream()
							.filter(Optional::isPresent)
							.map(Optional::get)
							.mapToDouble(vec -> vec.distanceTo(start))
							.min()
							.orElse(Double.POSITIVE_INFINITY);
				}
			}

			getBoundTypes(world, pos)
					.map(type -> new Hits(type, type.streamB()
							.map(aabb -> aabb.rayTrace(start, end))
							.collect(Collectors.toList())
					))
					.filter(hits -> hits.hits.stream().anyMatch(Optional::isPresent))
					.min(Comparator.comparingDouble(Hits::dist))
					.ifPresent(hit -> {
						original.hitInfo = hit.type;
						original.subHit = Streams.zip(
								Stream.iterate(-1, i -> i + 1),
								hit.hits.stream(),
								IndexedValue::new
						)
								.filter(i -> i.value.isPresent())
								.map(i -> new IndexedValue<>(i.index, i.value.get()))
								.min(Comparator.comparingDouble(a -> a.value.distanceTo(start)))
								.map(i -> i.index)
								.orElse(-1);
					});
		}

		return original;
	}
}
