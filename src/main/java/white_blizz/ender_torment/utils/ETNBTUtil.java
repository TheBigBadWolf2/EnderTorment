package white_blizz.ender_torment.utils;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;

@SuppressWarnings("SameParameterValue")
public class ETNBTUtil {

	public interface NullConsumer<T> extends Consumer<T>, Runnable {
		@Override default void run() { accept(null); }
		@Override void accept(@Nullable T t);
	}

	public static class NBTHelperBase<NBT> {
		protected final int type;
		protected final BiFunction<CompoundNBT, String, NBT> mapper;
		protected final Function<NBT, INBT> reverse;

		protected NBTHelperBase(int type, BiFunction<CompoundNBT, String, NBT> mapper, Function<NBT, INBT> reverse) {
			this.type = type;
			this.mapper = mapper;
			this.reverse = reverse;
		}

		public Optional<NBT> tryGet(CompoundNBT tag, String name) {
			if (tag.contains(name, type)) return Optional.of(mapper.apply(tag, name));
			return Optional.empty();
		}

		public void tryRun(CompoundNBT tag, String name, Consumer<NBT> consumer) {
			tryGet(tag, name).ifPresent(consumer);
		}

		public void tryRun(CompoundNBT tag, String name, Consumer<NBT> consumer, Runnable failed) {
			Optional<NBT> tryGet = tryGet(tag, name);
			if (tryGet.isPresent()) consumer.accept(tryGet.get());
			else failed.run();
		}

		public void tryRun2(CompoundNBT tag, String name, NullConsumer<NBT> consumer) {
			tryRun(tag, name, consumer, consumer);
		}

		public <T> Optional<T> tryGetAs(CompoundNBT tag, String name, Function<NBT, T> mapper) {
			return tryGet(tag, name).map(mapper);
		}


		public <T> void tryRunAs2(CompoundNBT tag, String name, Function<NBT, T> mapper, NullConsumer<T> consumer) {
			tryRunAs(tag, name, mapper, consumer, consumer);
		}

		public <T> void tryRunAs(CompoundNBT tag, String name, Function<NBT, T> mapper, Consumer<T> consumer) {
			tryGetAs(tag, name, mapper).ifPresent(consumer);
		}

		public <T> void tryRunAs(CompoundNBT tag, String name, Function<NBT, T> mapper, Consumer<T> consumer, Runnable failed) {
			Optional<T> tryGet = tryGetAs(tag, name, mapper);
			if (tryGet.isPresent()) consumer.accept(tryGet.get());
			else failed.run();
		}
	}

	public static class NBTHelper<NBT extends INBT> extends NBTHelperBase<NBT> {
		public NBTHelper(int type, BiFunction<CompoundNBT, String, NBT> mapper) {
			super(type, mapper, nbt -> nbt);
		}
	}

	public static class NBTMapper<NBT extends INBT, T> {
		private final NBTHelper<NBT> base;
		private final String defaultName;
		private final Function<NBT, T> deserializer;
		private final Function<T, NBT> serializer;

		public NBTMapper(NBTHelper<NBT> base, String defaultName, Function<NBT, T> deserializer, Function<T, NBT> serializer) {
			this.base = base;
			this.defaultName = defaultName;
			this.deserializer = deserializer;
			this.serializer = serializer;
		}

		public T getAs(CompoundNBT tag, String name, Supplier<T> factory) {
			return base.tryGetAs(tag, name, deserializer).orElseGet(() -> {
				T t = factory.get();
				if (serializer != null)
					tag.put(name, serializer.apply(t));
				return t;
			});
		}
		public T getAs(CompoundNBT tag) {
			return base.tryGetAs(tag, defaultName, deserializer).orElse(null);
		}

		public void tryRunAs(CompoundNBT tag, Consumer<T> consumer) { tryRunAs(tag, defaultName, consumer); }
		public void tryRunAs(CompoundNBT tag, Consumer<T> consumer, Runnable failed) { tryRunAs(tag, defaultName, consumer, failed); }
		public void tryRunAs2(CompoundNBT tag, NullConsumer<T> consumer) { tryRunAs2(tag, defaultName, consumer); }


		public void tryRunAs(CompoundNBT tag, String name, Consumer<T> consumer) {
			base.tryRunAs(tag, name, deserializer, consumer);
		}

		public void tryRunAs(CompoundNBT tag, String name, Consumer<T> consumer, Runnable failed) {
			base.tryRunAs(tag, name, deserializer, consumer, failed);
		}

		public void tryRunAs2(CompoundNBT tag, String name, NullConsumer<T> consumer) {
			base.tryRunAs2(tag, name, deserializer, consumer);
		}

		public boolean put(CompoundNBT tag, T value) { return put(tag, defaultName, value); }

		public boolean put(CompoundNBT tag, String name, T value) {
			if (value != null) {
				tag.put(name, serializer.apply(value));
				return true;
			}
			return false;
		}
	}

	public static final NBTHelper<CompoundNBT> COMPOUND = new NBTHelper<>(Constants.NBT.TAG_COMPOUND, CompoundNBT::getCompound);
	public static final NBTHelperBase<String> STRING = new NBTHelperBase<>(Constants.NBT.TAG_STRING, CompoundNBT::getString, StringNBT::valueOf);
	public static final NBTHelperBase<Integer> INT = new NBTHelperBase<>(Constants.NBT.TAG_INT, CompoundNBT::getInt, IntNBT::valueOf);
	public static final NBTHelperBase<Long> LONG = new NBTHelperBase<>(Constants.NBT.TAG_INT, CompoundNBT::getLong, LongNBT::valueOf);
	public static final NBTHelperBase<Boolean> BOOL = new NBTHelperBase<>(Constants.NBT.TAG_BYTE, CompoundNBT::getBoolean, ByteNBT::valueOf);

	public static final NBTMapper<CompoundNBT, UUID> UUID = new NBTMapper<>(COMPOUND, "id", NBTUtil::readUniqueId, NBTUtil::writeUniqueId);
	public static final NBTMapper<CompoundNBT, BlockPos> POS = new NBTMapper<>(COMPOUND, "pos", NBTUtil::readBlockPos, NBTUtil::writeBlockPos);
	public static final NBTMapper<CompoundNBT, BlockState> STATE = new NBTMapper<>(COMPOUND, "state", NBTUtil::readBlockState, NBTUtil::writeBlockState);



	public interface RSToObj<T> extends Function<String, T> {
		T rsFunc(ResourceLocation loc);
		@Override default T apply(String s) { return rsFunc(new ResourceLocation(s)); }
	}

	public static <K, V> ListNBT serializeMap(Map<K, V> map,
											  Function<K, INBT> keySerialize,
											  Function<V, INBT> valueSerialize) {
		return map.entrySet().stream().map(kvEntry -> {
			CompoundNBT tag = new CompoundNBT();
			tag.put("key", keySerialize.apply(kvEntry.getKey()));
			tag.put("value", valueSerialize.apply(kvEntry.getValue()));
			return tag;
		}).collect(ListNBT::new, ListNBT::add, ListNBT::addAll);
	}
	public static <K, V> void deserializeMap(ListNBT list,
											 Map<K, V> map,
											 Function<INBT, K> keyDeserialize,
											 Function<INBT, V> valueDeserialize) {
		map.clear();
		list.forEach(nbt -> {
			CompoundNBT tag = (CompoundNBT) nbt;
			map.put(keyDeserialize.apply(tag.get("key")), valueDeserialize.apply(tag.get("value")));
		});
	}

	public static <K, V> void deserializeMapCC(ListNBT list,
											 Map<K, V> map,
											 Function<CompoundNBT, K> keyDeserialize,
											 Function<CompoundNBT, V> valueDeserialize) {
		map.clear();
		list.forEach(nbt -> {
			CompoundNBT tag = (CompoundNBT) nbt;
			map.put(keyDeserialize.apply(tag.getCompound("key")), valueDeserialize.apply(tag.getCompound("value")));
		});
	}

	public static class NBTConverter<T> {
		private interface NodeBase<T> {
			void write(CompoundNBT tag, T target);
			void read(CompoundNBT tag, T target);
		}

		private static class Node<T, V> implements NodeBase<T> {
			private final String name;
			private final V defaultValue;
			private final NBTHelperBase<V> helper;
			private final Function<T, V> getter;
			private final BiConsumer<T, V> setter;

			private Node(String name, V defaultValue, NBTHelperBase<V> helper, Function<T, V> getter, BiConsumer<T, V> setter) {
				this.name = name;
				this.defaultValue = defaultValue;
				this.helper = helper;
				this.getter = getter;
				this.setter = setter;
			}

			@Override
			public void write(CompoundNBT tag, T target) {
				tag.put(name, helper.reverse.apply(getter.apply(target)));
			}
			@Override
			public void read(CompoundNBT tag, T target) {
				if (tag.contains(name, helper.type)) {
					setter.accept(target, helper.mapper.apply(tag, name));
				} else setter.accept(target, defaultValue);
			}
		}

		private final List<NodeBase<T>> nodes = new ArrayList<>();

		public NBTConverter<T> addInt(String name, int defaultValue, Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
			nodes.add(new Node<>(name, defaultValue, INT, getter, setter));
			return this;
		}
		public NBTConverter<T> addLong(String name, long defaultValue, Function<T, Long> getter, BiConsumer<T, Long> setter) {
			nodes.add(new Node<>(name, defaultValue, LONG, getter, setter));
			return this;
		}
		public NBTConverter<T> addBool(String name, boolean defaultValue, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
			nodes.add(new Node<>(name, defaultValue, BOOL, getter, setter));
			return this;
		}

		public CompoundNBT write(T target) {
			CompoundNBT tag = new CompoundNBT();
			nodes.forEach(node -> node.write(tag, target));
			return tag;
		}

		public void read(CompoundNBT tag, T target) {
			nodes.forEach(node -> node.read(tag, target));
		}
	}
}
