package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import white_blizz.ender_torment.common.conduit.*;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.conduit.io.IConduitBuffer;
import white_blizz.ender_torment.common.conduit.io.IConduitIO;
import white_blizz.ender_torment.common.conduit.io.IConduitInput;
import white_blizz.ender_torment.common.conduit.io.IConduitOutput;
import white_blizz.ender_torment.utils.Cashe;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConduitTE extends ETTileEntity implements ILinkable {

	public ConduitTE() { super(ETBlocks.CONDUIT_TYPE); }

	private final BlockMap links = new BlockMap();

	private enum IOType {
		IN, BUF, OUT
	}



	private class ANode<C> extends Node<C> implements IConduitIO<C>,
			IConduitInput<C>,
			IConduitBuffer<C>,
			IConduitOutput<C>
	{
		private final C cap;
		private final IOType ioType;
		@SuppressWarnings("rawtypes") private final Cashe cashe;

		protected ANode(Link<C> parent, DimensionType dim, BlockPos pos, IOType ioType, C cap) {
			super(parent, dim, pos);
			this.ioType = ioType;
			this.cap = cap;
			cashe = new Cashe<>(ConduitTE.this::getWorld, pos, World::getTileEntity, TileEntity::isRemoved);
		}

		@Override
		public Optional<IConduitInput<C>> asInput() {
			if (ioType == IOType.IN) return Optional.of(this);
			return Optional.empty();
		}

		@Override
		public Optional<IConduitBuffer<C>> asBuffer() {
			if (ioType == IOType.BUF) return Optional.of(this);
			return Optional.empty();
		}

		@Override
		public Optional<IConduitOutput<C>> asOutput() {
			if (ioType == IOType.OUT) return Optional.of(this);
			return Optional.empty();
		}

		@Override public Optional<IConduitIO<C>> asIO() { return Optional.of(this); }

		private boolean valid = true;

		@Override public void invalidate() { valid = false; }

		@Override public C getCap() { return cap; }

		@Override
		protected boolean validate() {
			return valid && !cashe.hasChanged()/*world != null && world.getBlockState(getPos()).equals(cashe.getState())*/;
		}
	}

	private class ALink<C> extends Link<C> {
		private final Object2BooleanArrayMap<Direction> directions = new Object2BooleanArrayMap<>();

		protected ALink(DimensionType dim, BlockPos pos, ConduitType<C> type, boolean isClient) {
			super(dim, pos, type, isClient);
		}

		@Override
		protected Node<C> makeNode(DimensionType dim, BlockPos pos, @Nullable Direction dir, C c) {
			IOType ioType = IOType.BUF;
			if (dir == Direction.UP) ioType = IOType.IN;
			else if (dir == Direction.DOWN) ioType = IOType.OUT;
			return new ANode<>(this, dim, pos, ioType, c);
		}

		@Override protected boolean canConnectTo(Direction dir) {
			return directions.computeIfAbsent(dir, d -> true);
		}

		@Override protected CompoundNBT serializeExtraNBT(CompoundNBT tag) {
			tag.put("directions", this.directions
					.object2BooleanEntrySet()
					.stream()
					.map((e) -> {
						CompoundNBT t = new CompoundNBT();
						t.putString("key", e.getKey().getName());
						t.putBoolean("value", e.getBooleanValue());
						return t;
					}).collect(ListNBT::new, ListNBT::add, ListNBT::addAll)
			);
			return tag;
		}
		@Override protected void deserializeExtraNBT(CompoundNBT tag) {
			directions.clear();
			tag.getList("directions", Constants.NBT.TAG_COMPOUND).forEach(nbt -> {
				CompoundNBT t = (CompoundNBT) nbt;
				directions.put(Direction.byName(t.getString("key")), t.getBoolean("value"));
			});
		}

		@Override protected void markDirty() { ConduitTE.this.markDirty(); }
	}

	@Override
	public boolean addType(ConduitType<?> type) {
		World world = getWorld();
		if (world == null) return false;
		if (links.containsKey(type)) return false;

		Link<?> link = new ALink<>(world.dimension.getType(), getPos(), type, world.isRemote);
		links.add(link);
		Network.addLink(link);
		link.updateNetwork();
		markDirty();
		return true;
	}

	@Override
	public boolean removeType(ConduitType<?> type) {
		/*World world = getWorld();
		if (world == null) return false;*/
		Link<?> link = links.get(type);
		if (link != null) {
			links.remove(link);
			link.remove(true);
			markDirty();
			return true;
		}
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		links.clear(true, true);
	}

	public void checkNeighbors() {
		//EnderTorment.LOGGER.info("Updating @ {}", pos);
		links.values().forEach(Link::updateNetwork);
	}

	@Override
	public void onLoad() {

	}

	public void checkNeighbor(BlockPos pos) {
		links.values()
				.stream()
				.flatMap(link -> link.getNodes().values().stream())
				.filter(node -> node.getPos().equals(pos))
				.forEach(Node::invalidate);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos);
	}

	@Nullable @Override
	public <C> Link<C> getLink(ConduitType<C> type) {
		return links.get(type);
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public Map<ConduitType<?>, Link<?>> getLinks() {
		return ImmutableMap.copyOf(links);
	}

	@Override
	public <C> boolean setConnection(Direction side, ConduitType<C> type, boolean on) {
		Link<C> link = links.get(type);
		if (link == null) return false;
		if (link instanceof ALink) {
			ALink<C> l = (ALink<C>) link;
			boolean oldValue = l.directions.getOrDefault(side, true);
			return set(l, type, side, oldValue, on);
			/*if (l.directions.containsKey(side)) {
				boolean value = l.directions.getBoolean(side);
				if (on != value) {
					l.directions.put(side, on);
					l.updateNetwork();
					markDirty();
					return true;
				}
			} else {
				l.directions.put(side, on);
				l.updateNetwork();
				markDirty();
				return !on;
			}*/
		}
		return false;
	}

	private <C> boolean set(ALink<C> lThis, ConduitType<C> type, Direction side, boolean oldValue, boolean newValue) {
		if (world != null) {
			Optional<? extends ConduitTE> otherOpt = ETTileEntity.get(getClass(), world, pos.offset(side));
			if (otherOpt.isPresent()) {
				ConduitTE other = otherOpt.get();
				Link<C> link = other.links.get(type);
				if (link instanceof ALink) {
					ALink<C> lThat = (ALink<C>) link;
					boolean oldThatValue = lThat.directions.getOrDefault(side.getOpposite(), true);
					return set(lThis, lThat, side, oldValue, oldThatValue, newValue);
				}
			}
		}
		if (newValue != oldValue) {
			lThis.directions.put(side, newValue);
			lThis.updateNetwork();
			markDirty();
			return true;
		}
		return false;
	}

	private <C> boolean set(
			ALink<C> lThis, ALink<C> lThat, Direction side,
			boolean oldThisValue, boolean oldThatValue, boolean newValue) {
		if (oldThisValue != newValue || oldThatValue != newValue) {
			if (oldThisValue != newValue) {
				lThis.directions.put(side, newValue);
				lThis.updateNetwork();
				lThis.markDirty();
			}
			if (oldThatValue != newValue) {
				lThat.directions.put(side.getOpposite(), newValue);
				lThat.updateNetwork();
				lThat.markDirty();
			}
			return true;
		}
		return false;
	}

		@Override protected void extraRead(CompoundNBT tag) {
		links.clear(true, false);
		boolean isClient;
		if (world != null) isClient = world.isRemote();
		else isClient = EffectiveSide.get().isClient();
		ListNBT list = tag.getList("links", Constants.NBT.TAG_COMPOUND);
		list.forEach((nbt) -> {
			ALink<?> link = Link.deserialize((CompoundNBT) nbt, ALink::new, isClient);
			Network.addLinkLazy(link);
			//if (world != null) link.updateNetwork();
			links.add(link);
		});
	}
	@Override protected void extraWrite(CompoundNBT tag) {
		ListNBT list = new ListNBT();
		for (BlockMap.TypeLink<?> link : links) {
			list.add(link.getValue().serializeNBT());
		}
		tag.put("links", list);
	}

	@Override protected List<Cap<?>> getCaps() { return Lists.newArrayList(); }

	/*public Collection<Link> getLinks() {
		return links.values();
	}*/
}
