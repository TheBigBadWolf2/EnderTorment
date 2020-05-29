package white_blizz.ender_torment.common;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.utils.Ref;

import java.util.Comparator;
import java.util.HashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ETRegistry {
	public static IForgeRegistry<ConduitType<?>> CONDUIT_TYPE;

	private static final Object2IntArrayMap<ConduitType<?>> SORT = new Object2IntArrayMap<>();

	public static float getSortValue(ConduitType<?> type) {
		int sort = SORT.getInt(type);
		int size = SORT.size();
		return sort - (size / 2F) + 0.5F;
	}

	public static double getSortValue(ConduitType<?> type, double scale) {
		int sort = SORT.getInt(type);
		int size = SORT.size();
		return (sort - (size / 2D) + 0.5D) * scale;
	}

	public static void register(RegistryEvent.NewRegistry evt) {
		CONDUIT_TYPE = new RegistryBuilder()
				.setType(ConduitType.class)
				.add((IForgeRegistry.ClearCallback) (owner, stage) -> SORT.clear())
				.add((IForgeRegistry.BakeCallback) (owner, stage) -> {
					class S {
						final ConduitType<?> type;
						final int sort;

						S(ConduitType<?> type) {
							this.type = type;
							sort = type.hashCode();
						}
					}
					ConduitType<?>[] conduitTypes = CONDUIT_TYPE.getValues()
							.stream()
							.map(S::new)
							.sorted(Comparator.comparingInt(a -> a.sort))
							.map(s -> s.type)
							.toArray(ConduitType[]::new);

					for (int i = 0; i < conduitTypes.length; i++) {
						ConduitType<?> type = conduitTypes[i];
						SORT.put(type, i);
					}
				})
				.setName(Ref.MOD.rl.loc("conduit_type"))
				.create();
	}
}
