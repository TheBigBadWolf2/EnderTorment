package white_blizz.ender_torment.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

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


}
