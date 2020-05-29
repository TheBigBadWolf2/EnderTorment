package white_blizz.ender_torment.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("SameParameterValue")
public class ETNBTUtil {
	public static class NBTHelper<NBT> {
		private final int type;
		private final BiFunction<CompoundNBT, String, NBT> mapper;

		private NBTHelper(int type, BiFunction<CompoundNBT, String, NBT> mapper) {
			this.type = type;
			this.mapper = mapper;
		}

		public Optional<NBT> tryGet(CompoundNBT tag, String name) {
			if (tag.contains(name, type)) return Optional.of(mapper.apply(tag, name));
			return Optional.empty();
		}

		public void tryRun(CompoundNBT tag, String name, Consumer<NBT> consumer) {
			tryGet(tag, name).ifPresent(consumer);
		}

		public <T> Optional<T> tryGetAs(CompoundNBT tag, String name, Function<NBT, T> mapper) {
			return tryGet(tag, name).map(mapper);
		}

		public <T> void tryRunAs(CompoundNBT tag, String name, Function<NBT, T> mapper, Consumer<T> consumer) {
			tryGetAs(tag, name, mapper).ifPresent(consumer);
		}
	}

	public static final NBTHelper<CompoundNBT> COMPOUND = new NBTHelper<>(Constants.NBT.TAG_COMPOUND, CompoundNBT::getCompound);
	public static final NBTHelper<String> STRING = new NBTHelper<>(Constants.NBT.TAG_STRING, CompoundNBT::getString);

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
}
