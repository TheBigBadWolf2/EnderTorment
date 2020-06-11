package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import white_blizz.ender_torment.utils.ETUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ETTileEntity extends TileEntity implements IETTileEntity {
	public <T extends ETTileEntity> ETTileEntity(Supplier<TileEntityType<T>> type) { super(type.get()); }

	private boolean isDirty = false;

	protected boolean shouldAutoMark() {
		return true;
	}

	@Override
	public void markDirty() {
		if (shouldAutoMark()) {
			isDirty = true;
			mark();
		} else isDirty = true;
	}

	protected void mark() {
		if (isDirty && world != null && !world.isRemote) {
			super.markDirty();
			world.markAndNotifyBlock(getPos(), null,
					getBlockState(), getBlockState(),
					Constants.BlockFlags.DEFAULT);
		}
		isDirty = false;
	}

	@Override
	public final void read(CompoundNBT compound) {
		extraRead(compound);
		super.read(compound);
	}

	@Override
	public final CompoundNBT write(CompoundNBT compound) {
		compound = super.write(compound);
		extraWrite(compound);
		return compound;
	}

	private CompoundNBT write() { return write(new CompoundNBT()); }

	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write();
	}

	@Override public void handleUpdateTag(CompoundNBT tag) { read(tag); }

	protected abstract void extraRead(CompoundNBT tag);
	protected abstract void extraWrite(CompoundNBT tag);

	protected static class Cap<C> {
		private static <C> Map<Direction, NonNullSupplier<LazyOptional<C>>> convert(Map<Direction, NonNullSupplier<C>> suppliers) {
			//noinspection UnstableApiUsage
			return suppliers.entrySet().stream()
					.map(entry -> new Tuple<Direction, NonNullSupplier<LazyOptional<C>>>(
							entry.getKey(), () -> LazyOptional.of(entry.getValue()
					))).collect(ImmutableMap.toImmutableMap(Tuple::getA, Tuple::getB));
		}

		private final Capability<C> cap;
		private final NonNullSupplier<LazyOptional<C>> supplier;
		private final Map<Direction, NonNullSupplier<LazyOptional<C>>> suppliers;

		private Cap(Capability<C> cap) {
			this.cap = cap;
			this.supplier = null;
			this.suppliers = null;
		}

		public Cap(Capability<C> cap, NonNullSupplier<LazyOptional<C>> supplier) {
			this.cap = cap;
			this.supplier = supplier;
			this.suppliers = null;
		}
		public Cap(Capability<C> cap, NonNullSupplier<LazyOptional<C>> supplier, Map<Direction, NonNullSupplier<LazyOptional<C>>> suppliers) {
			this.cap = cap;
			this.supplier = supplier;
			this.suppliers = suppliers;
		}

		public Cap(Capability<C> cap, Map<Direction, NonNullSupplier<LazyOptional<C>>> suppliers) {
			this.cap = cap;
			this.supplier = null;
			this.suppliers = suppliers;
		}
		/*public Cap(Capability<C> cap, NonNullSupplier<C> supplier) {
			this.cap = cap;
			this.supplier = () -> LazyOptional.of(supplier);
			this.suppliers = null;
		}

		public Cap(Capability<C> cap, NonNullSupplier<C> supplier, Map<Direction, NonNullSupplier<C>> suppliers) {
			this.cap = cap;
			this.supplier = () -> LazyOptional.of(supplier);
			this.suppliers = convert(suppliers);
		}

		public Cap(Capability<C> cap, Map<Direction, NonNullSupplier<C>> suppliers) {
			this.cap = cap;
			this.supplier = null;
			this.suppliers = convert(suppliers);
		}*/

		private Optional<NonNullSupplier<LazyOptional<C>>> get(@Nullable Direction side) {
			if (suppliers == null) return Optional.ofNullable(supplier);
			if (side == null) return Optional.ofNullable(supplier);
			NonNullSupplier<LazyOptional<C>> supplier = suppliers.getOrDefault(side, null);
			return Optional.ofNullable(supplier);
		}

		private <T> Optional<LazyOptional<T>> check(@Nonnull Capability<T> cap, @Nullable Direction side) {
			return get(side).map(sup -> this.cap.orEmpty(cap, sup.get()));
		}
	}

	protected static class CapList implements List<Cap<?>> {
		public static ListBuilder New() { return new ListBuilder(); }

		public static class ListBuilder {

			public class CapBuilder<C> {
				private final Capability<C> cap;
				private final Map<Direction, NonNullSupplier<LazyOptional<C>>> suppliers = new HashMap<>();
				private NonNullSupplier<LazyOptional<C>> supplier = null;
				private CapBuilder(Capability<C> cap) { this.cap = cap; }

				public CapBuilder<C> mapS(@Nullable Direction side, NonNullSupplier<C> sup) {
					if (side == null) supplier = () -> LazyOptional.of(sup);
					else suppliers.put(side, () -> LazyOptional.of(sup));
					return this;
				}

				public CapBuilder<C> mapS(Function<Direction, NonNullSupplier<C>> mapper) {
					supplier = () -> LazyOptional.of(mapper.apply(null));
					Arrays.stream(Direction.values())
							.forEach(side -> suppliers.compute(side, (k, o) -> () -> LazyOptional.of(mapper.apply(k))));
					return this;
				}

				public <CNext> CapBuilder<CNext> next(Capability<CNext> cap) { return make().newCap(cap); }

				private ListBuilder add(Cap<C> cap) {
					list.add(cap);
					return CapList.ListBuilder.this;
				}

				public ListBuilder make() {
					if (supplier == null && suppliers.isEmpty())
						return add(new Cap<>(cap));
					if (supplier != null && suppliers.isEmpty())
						return add(new Cap<>(cap, supplier));
					if (supplier == null)
						return add(new Cap<>(cap, suppliers));
					return add(new Cap<>(cap, supplier, suppliers));
				}

				public CapList done() { return make().build(); }
			}

			private final List<Cap<?>> list = new ArrayList<>();

			private ListBuilder() {}

			public <C> CapBuilder<C> newCap(Capability<C> cap) { return new CapBuilder<>(cap); }

			public <C> ListBuilder addCapS(Capability<C> cap, NonNullSupplier<C> supplier) {
				list.add(new Cap<>(cap, () -> LazyOptional.of(supplier)));
				return this;
			}
			public <C> ListBuilder addCapL(Capability<C> cap, NonNullSupplier<LazyOptional<C>> supplier) {
				list.add(new Cap<>(cap, supplier));
				return this;
			}

			public <C> ListBuilder addCapS(Capability<C> cap, NonNullSupplier<C> supplier, Map<Direction, NonNullSupplier<C>> suppliers) {
				list.add(new Cap<>(cap, () -> LazyOptional.of(supplier), Cap.convert(suppliers)));
				return this;
			}
			public <C> ListBuilder addCapS(Capability<C> cap, Map<Direction, NonNullSupplier<C>> suppliers) {
				list.add(new Cap<>(cap, Cap.convert(suppliers)));
				return this;
			}

			public CapList build() { return new CapList(ImmutableList.copyOf(list)); }
		}

		private final List<Cap<?>> list;
		private CapList(List<Cap<?>> list) { this.list = list; }

		@Override public int size() { return list.size(); }
		@Override public boolean isEmpty() { return list.isEmpty(); }
		@Override public boolean contains(Object o) { return list.contains(o); }
		@Override public Iterator<Cap<?>> iterator() { return list.iterator(); }
		@Override public Object[] toArray() { return list.toArray(); }
		@SuppressWarnings("SuspiciousToArrayCall")
		@Override public <T> T[] toArray(T[] a) { return list.toArray(a); }
		@Override public boolean containsAll(Collection<?> c) { return list.containsAll(c); }
		@Override public Cap<?> get(int index) { return list.get(index); }
		@Override public int indexOf(Object o) { return list.indexOf(o); }
		@Override public int lastIndexOf(Object o) { return list.lastIndexOf(o); }
		@Override public ListIterator<Cap<?>> listIterator() { return list.listIterator(); }
		@Override public ListIterator<Cap<?>> listIterator(int index) { return list.listIterator(index); }
		@Override public List<Cap<?>> subList(int fromIndex, int toIndex) { return list.subList(fromIndex, toIndex); }

		@Override public boolean add(Cap<?> cap) { throw new UnsupportedOperationException(); }
		@Override public boolean remove(Object o) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(Collection<? extends Cap<?>> c) { throw new UnsupportedOperationException(); }
		@Override public boolean addAll(int index, Collection<? extends Cap<?>> c) { throw new UnsupportedOperationException(); }
		@Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
		@Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
		@Override public void clear() { throw new UnsupportedOperationException(); }
		@Override public Cap<?> set(int index, Cap<?> element) { throw new UnsupportedOperationException(); }
		@Override public void add(int index, Cap<?> element) { throw new UnsupportedOperationException(); }
		@Override public Cap<?> remove(int index) { throw new UnsupportedOperationException(); }
	}

	protected abstract List<Cap<?>> getCaps();

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return getCaps().stream()
				.filter(cap1 -> cap1.cap == cap)
				.findFirst()
				.map(cap1 -> cap1.check(cap, side))
				.flatMap(Function.identity())
				.orElse(super.getCapability(cap, side));
	}



	public static <T extends ETTileEntity> Optional<T> get(
			Class<T> clazz, IBlockReader world,
			BlockPos pos) {
		return IETTileEntity.get(clazz, world, pos);
	}
}
