package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

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

	@Override
	public void markDirty() {
		if (world != null && !world.isRemote) {
			if (!isDirty) {
				super.markDirty();
				world.markAndNotifyBlock(getPos(), null,
						getBlockState(), getBlockState(),
						Constants.BlockFlags.DEFAULT);
				isDirty = true;
			}
		}
	}

	@Override
	public final void read(CompoundNBT compound) {
		extraRead(compound);
		super.read(compound);
	}

	@Override
	public final CompoundNBT write(CompoundNBT compound) {
		isDirty = false;
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
		private final Capability<C> cap;
		private final NonNullSupplier<C> supplier;
		private final Map<Direction, NonNullSupplier<C>> suppliers;

		private Cap(Capability<C> cap) {
			this.cap = cap;
			this.supplier = null;
			this.suppliers = null;
		}

		public Cap(Capability<C> cap, NonNullSupplier<C> supplier) {
			this.cap = cap;
			this.supplier = supplier;
			this.suppliers = null;
		}

		public Cap(Capability<C> cap, NonNullSupplier<C> supplier, Map<Direction, NonNullSupplier<C>> suppliers) {
			this.cap = cap;
			this.supplier = supplier;
			this.suppliers = suppliers;
		}

		public Cap(Capability<C> cap, Map<Direction, NonNullSupplier<C>> suppliers) {
			this.cap = cap;
			this.supplier = null;
			this.suppliers = suppliers;
		}

		private Optional<NonNullSupplier<C>> get(@Nullable Direction side) {
			if (suppliers == null) return Optional.ofNullable(supplier);
			if (side == null) return Optional.ofNullable(supplier);
			NonNullSupplier<C> supplier = suppliers.getOrDefault(side, null);
			return Optional.ofNullable(supplier);
		}

		private <T> Optional<LazyOptional<T>> check(@Nonnull Capability<T> cap, @Nullable Direction side) {
			return get(side).map(sup -> this.cap.orEmpty(cap, LazyOptional.of(sup)));
		}
	}

	protected static class CapList implements List<Cap<?>> {
		public static ListBuilder New() { return new ListBuilder(); }

		public static class ListBuilder {

			public class CapBuilder<C> {
				private final Capability<C> cap;
				private final Map<Direction, NonNullSupplier<C>> suppliers = new HashMap<>();
				private NonNullSupplier<C> supplier = null;
				private CapBuilder(Capability<C> cap) { this.cap = cap; }

				public CapBuilder<C> map(@Nullable Direction side, NonNullSupplier<C> sup) {
					if (side == null) supplier = sup;
					else suppliers.put(side, sup);
					return this;
				}

				public CapBuilder<C> map(Function<Direction, NonNullSupplier<C>> mapper) {
					supplier = mapper.apply(null);
					Arrays.stream(Direction.values())
							.forEach(side -> suppliers.compute(side, (k, o) -> mapper.apply(k)));
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

			public <C> ListBuilder addCap(Capability<C> cap, NonNullSupplier<C> supplier) {
				list.add(new Cap<>(cap, supplier));
				return this;
			}
			public <C> ListBuilder addCap(Capability<C> cap, NonNullSupplier<C> supplier, Map<Direction, NonNullSupplier<C>> suppliers) {
				list.add(new Cap<>(cap, supplier, suppliers));
				return this;
			}
			public <C> ListBuilder addCap(Capability<C> cap, Map<Direction, NonNullSupplier<C>> suppliers) {
				list.add(new Cap<>(cap, suppliers));
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
		for (Cap<?> cap1 : getCaps()) if (cap == cap1.cap) {
			Optional<LazyOptional<T>> check = cap1.check(cap, side);
			if (check.isPresent()) return check.get();
			break;
		}

		return super.getCapability(cap, side);
	}

	public static <T extends ETTileEntity> Optional<T> get(
			Class<T> clazz, IBlockReader world,
			BlockPos pos) {
		return IETTileEntity.get(clazz, world, pos);
	}
}
