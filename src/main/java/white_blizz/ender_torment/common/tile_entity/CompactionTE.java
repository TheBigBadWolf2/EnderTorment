package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.Streams;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.compaction.Compaction;
import white_blizz.ender_torment.common.compaction.ICompaction;
import white_blizz.ender_torment.common.compaction.IWorldCompaction;
import white_blizz.ender_torment.utils.ETNBTUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CompactionTE extends ETTileEntity implements ITickableTileEntity {
	public static Optional<CompactionTE> get(
			IBlockReader world,
			BlockPos pos) {
		return get(CompactionTE.class, world, pos);
	}

	private static final int FLAGS = 2 | 16 | 32;
	@CapabilityInject(ICompaction.class)
	public static Capability<ICompaction> COMPACTION;
	@CapabilityInject(IWorldCompaction.class)
	public static Capability<IWorldCompaction> WORLD_COMPACTION;

	private interface IData extends INBTSerializable<CompoundNBT> {
		BlockPos rel();
		@Nullable UUID getID();
		@Nullable ICompaction getCompaction();
	}
	private class Master implements IData {
		private ICompaction compaction;
		private UUID id;
		private Master() {}

		@Override public CompoundNBT serializeNBT() {
			CompoundNBT tag =  new CompoundNBT();
			tag.putBoolean("slave", false);
			ETNBTUtil.UUID.put(tag, "id", id);
			return tag;
		}
		@Override public void deserializeNBT(CompoundNBT tag) {
			ETNBTUtil.UUID.tryRunAs2(tag, "id", id -> this.id = id);
		}

		@Override public BlockPos rel() { return pos; }

		@Nullable
		@Override public UUID getID() { return id; }

		@Nullable
		@Override
		public ICompaction getCompaction() {
			if (compaction == null) {
				if (world != null) {
					MinecraftServer server = world.getServer();
					if (server != null) {
						server.getWorld(DimensionType.OVERWORLD).getCapability(WORLD_COMPACTION).ifPresent(c -> compaction = c.get(id));
					}
				}
			}
			return compaction;
		}
	}
	private class Slave implements IData {
		private Slave() {}


		private ICompaction compaction = null;
		private UUID id;
		private CompactionTE master;
		private BlockPos pos;

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean("slave", true);
			ETNBTUtil.POS.put(tag, pos);
			ETNBTUtil.UUID.put(tag, "id", id);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundNBT tag) {
			ETNBTUtil.POS.tryRunAs2(tag, pos -> this.pos = pos);
			ETNBTUtil.UUID.tryRunAs2(tag, "id", id -> this.id = id);
		}

		@Nullable
		private CompactionTE getMaster() {
			if (master == null && world != null)
				get(world, pos).ifPresent(m -> master = m);
			return master;
		}

		@Nullable
		private <T> T fromMaster(Function<CompactionTE, T> func) {
			CompactionTE master = getMaster();
			if (master != null) return func.apply(master);
			else return null;
		}

		@Override public BlockPos rel() { return pos; }

		@Nullable @Override
		public UUID getID() {
			if (id == null) id = fromMaster(CompactionTE::getID);
			return id;
		}

		@Nullable @Override
		public ICompaction getCompaction() {
			if (compaction == null) compaction = fromMaster(CompactionTE::getCompaction);
			if (compaction == null && world != null) {
				MinecraftServer server = world.getServer();
				if (server != null) server.getWorld(DimensionType.OVERWORLD).getCapability(WORLD_COMPACTION).ifPresent(c -> compaction = c.get(id));
			}
			return compaction;
		}
	}

	/*@Nullable private BlockPos master;
	@Nullable */
	private IData data;
	private ICompaction.BlockInfo info;
	private boolean contracting = true;
	private final int time = 20 * 5;
	private int progression = time, progressionLast = time;

	public CompactionTE() { super(ETBlocks.COMPACTION_TYPE); }

	@Nullable public UUID getID() { return data.getID(); }
	@Nullable public ICompaction getCompaction() { return data.getCompaction(); }

	private Integer floor = null/*, ceil = null*/;

	public void setMaster(CompactionTE master) {
		Slave slave = new Slave();
		slave.master = master;
		slave.id = master.getID();
		int y = pos.getY();
		if (master.floor == null) master.floor = y;
		else master.floor = Math.min(master.floor, y);
		/*if (master.ceil == null) master.ceil = y;
		else master.ceil = Math.min(master.ceil, y);*/
		slave.pos = master.pos.toImmutable();
		data = slave;
		markDirty();
	}

	private int getFloor() {
		if (floor == null) {
			if (data instanceof Slave) floor = ((Slave) data).fromMaster(m -> m.floor);
			if (floor == null) return 0;
		}
		return floor;
	}

	/*public CompactionTE setAsMaster(UUID id) {
		Master master = new Master();
		master.id = id;
		data = master;
		return this;
	}*/

	public CompactionTE setAsMaster(ICompaction compaction) {
		Master master = new Master();
		master.compaction = compaction;
		master.id = compaction.getID();
		data = master;
		markDirty();
		return this;
	}

	public float getProgression(float partialTicks) {
		return MathHelper.lerp(partialTicks, progressionLast, progression);
	}

	public float getProgressionPercent(float partialTicks) {
		return getProgression(partialTicks) / time;
	}

	@Override
	protected void extraRead(CompoundNBT tag) {
		ETNBTUtil.COMPOUND.tryRunAs2(tag, "data", data -> {
			Supplier<IData> factory;
			if (data.getBoolean("slave")) factory = Slave::new;
			else factory = Master::new;
			IData d = factory.get();
			d.deserializeNBT(data);
			return d;
		}, d -> this.data = d);
		ETNBTUtil.COMPOUND.tryRunAs2(tag, "info", ICompaction.BlockInfo::new, bi -> info = bi);
		contracting = tag.getBoolean("contracting");
		progression = tag.getInt("progression");
		progressionLast = tag.getInt("progressionLast");
		ETNBTUtil.INT.tryRun2(tag, "floor", i -> floor = i);
	}

	@Override
	protected void extraWrite(CompoundNBT tag) {
		if (data != null) tag.put("data", data.serializeNBT());
		if (info != null) tag.put("info", info.serializeNBT());
		tag.putBoolean("contracting", contracting);
		tag.putInt("progression", progression);
		tag.putInt("progressionLast", progressionLast);
		if (floor != null) tag.putInt("floor", floor);
	}

	@Override protected List<Cap<?>> getCaps() {
		return CapList.New().addCapL(COMPACTION, () -> {
			ICompaction compaction = getCompaction();
			if (compaction != null) return LazyOptional.of(() -> compaction);
			return LazyOptional.empty();
		}).build();
	}

	public Optional<BlockState> getState() {
		if (info == null) return Optional.empty();
		return Optional.of(info.getState());
	}

	private TileEntity cashed;

	public Optional<TileEntity> getTE() {
		if (cashed == null) {
			getState().filter(IForgeBlockState::hasTileEntity).map(state -> {
				TileEntity te = state.createTileEntity(world);
				if (te != null) {
					CompoundNBT tag = info.getTag();
					tag.putInt("x", pos.getX());
					tag.putInt("y", pos.getY());
					tag.putInt("z", pos.getZ());
					te.read(tag);
					ObfuscationReflectionHelper.setPrivateValue(
							TileEntity.class, te,
							state, "field_195045_e"
					);
				}
				return te;
			}).ifPresent(te -> cashed = te);
		}
		return Optional.ofNullable(cashed);
	}

	@Override protected boolean shouldAutoMark() { return false; }

	@Override
	public void tick() {
		if (world == null/* || world.isRemote()*/) return;
		if (data == null) {
			EnderTorment.LOGGER.fatal("Compaction Chamber at {} was missing data!", pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
			return;
		} else if (info == null) {
			EnderTorment.LOGGER.fatal("Compaction Chamber at {} was missing info!", pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
			return;
		}
		ICompaction compaction = getCompaction();
		if (progressionLast != progression) {
			progressionLast = progression;
			markDirty();
		}
		if (contracting) {
			if (progression == 0) {
				if (compaction != null) compaction.add(info);
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
				return;
			}
			if (progression > 0) {
				progression--;
				markDirty();
			}
		} else {
			if (progression == time) {
				info.tryPlace(world, data.rel(), FLAGS);
				if (compaction != null) compaction.remove(info.getPos(), data.rel());
				return;
			}
			if (progression < time) {
				progression++;
				markDirty();
			}
		}
		mark();
	}

	private void reverse() {
		progression = progressionLast = 0;
		contracting = false;
		markDirty();
	}

	public AxisAlignedBB getShape(float partialTicks) {
		float progression = getProgressionPercent(partialTicks);
		float floor = getFloor();

		float cur = pos.getY();

		floor -= cur;

		float myFloor = MathHelper.lerp(progression, floor, 0F);
		float myCeil = MathHelper.lerp(progression, floor, 1F);

		return new AxisAlignedBB(0, myFloor, 0, 1, myCeil, 1);
	}

	/*@Nullable
	public static CompactionTE set(IWorld world, BlockPos pos) {
		BlockInfo info = new BlockInfo(world, pos);
		if (info.state.isAir(world, pos)) return null;
		world.setBlockState(pos, ETBlocks.COMPACTION.get().getDefaultState(), FLAGS);
		CompactionTE te = get(CompactionTE.class, world, pos).orElseThrow(IllegalStateException::new);
		te.info = info;
		return te;
	}*/

	private static CompactionTE collect(World world, BlockPos pos, BlockPos rel, ICompaction compaction) {
		ICompaction.BlockInfo info = compaction.newInfo(world, pos);
		world.removeTileEntity(pos); //So it doesn't drop items.
		world.setBlockState(pos, ETBlocks.COMPACTION.get().getDefaultState(), FLAGS);
		CompactionTE te = get(world, pos).orElseThrow(IllegalStateException::new);
		te.info = info.relativeTo(rel);
		te.markDirty();
		return te;
	}

	public static void collect(World world, BlockPos pos, @SuppressWarnings("unused") Direction facing, UUID id, Iterable<BlockPos> poses) {
		MinecraftServer server = world.getServer();
		if (server == null) return;
		LazyOptional<IWorldCompaction> cap = server.getWorld(DimensionType.OVERWORLD).getCapability(WORLD_COMPACTION);
		if (!cap.isPresent()) return;
		IWorldCompaction worldCompaction = cap.orElseThrow(IllegalStateException::new);
		ICompaction compaction = worldCompaction.getOrElse(id, Compaction::new);
		if (!compaction.canCollect()) return;

		CompactionTE master = collect(world, pos, pos, compaction);
		master.setAsMaster(compaction);

		Streams.stream(poses)
				.filter(p -> !p.equals(pos))
				//.map(p -> collect(world, p, pos, compaction))
				.map(p -> compaction.newInfo(world, p))
				.filter(bi -> bi.isValid(world))
				.map(bi -> {
					world.removeTileEntity(bi.getPos()); //So it doesn't drop items.
					world.setBlockState(bi.getPos(), ETBlocks.COMPACTION.get().getDefaultState(), FLAGS);
					CompactionTE te = get(world, bi.getPos()).orElseThrow(IllegalStateException::new);
					te.info = bi.relativeTo(pos);
					te.markDirty();
					return te;
				})
				.forEach(te -> te.setMaster(master))
		;
	}

	private static CompactionTE release(World world, BlockPos pos) {
		world.setBlockState(pos, ETBlocks.COMPACTION.get().getDefaultState(), FLAGS);
		CompactionTE te = get(world, pos).orElseThrow(IllegalStateException::new);
		te.reverse();
		return te;
	}

	private static CompactionTE release(World world, ICompaction.BlockInfo info, BlockPos rel) {
		BlockPos pos = info.getPos(rel);
		world.setBlockState(pos, ETBlocks.COMPACTION.get().getDefaultState(), FLAGS);
		CompactionTE te = get(world, pos).orElseThrow(IllegalStateException::new);
		te.info = info.localFrom(rel);
		te.reverse();
		return te;
	}

	public static void release(World world, BlockPos pos, @SuppressWarnings("unused") Direction facing, UUID id) {
		MinecraftServer server = world.getServer();
		if (server == null) return;
		LazyOptional<IWorldCompaction> cap = server.getWorld(DimensionType.OVERWORLD).getCapability(WORLD_COMPACTION);
		if (!cap.isPresent()) return;
		IWorldCompaction worldCompaction = cap.orElseThrow(IllegalStateException::new);
		ICompaction compaction = worldCompaction.get(id);
		if (compaction == null || !compaction.canRelease()) return;

		CompactionTE master = release(world, pos).setAsMaster(compaction);
		compaction.infoList()
				.stream()
				.filter(bi -> bi.getPos().equals(BlockPos.ZERO))
				.findFirst().ifPresent(bi -> master.info = bi.localFrom(pos))
		;

		compaction.infoList()
				.stream()
				.filter(bi -> !bi.getPos().equals(BlockPos.ZERO))
				.map(bi -> release(world, bi, pos))
				.forEach(te -> te.setMaster(master))
		;
	}
}
