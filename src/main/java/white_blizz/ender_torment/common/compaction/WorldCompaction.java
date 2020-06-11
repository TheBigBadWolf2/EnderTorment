package white_blizz.ender_torment.common.compaction;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class WorldCompaction implements IWorldCompaction {
	private final Map<UUID, ICompaction> map = new HashMap<>();

	@Override
	public ListNBT serializeNBT() {
		return map.values()
				.stream()
				.map(INBTSerializable::serializeNBT)
				.collect(ListNBT::new, ListNBT::add, ListNBT::addAll);
	}

	@Override
	public void deserializeNBT(ListNBT list) {
		map.clear();
		list.stream()
				.map(tag -> (CompoundNBT)tag)
				.map(tag -> {
					Compaction comp = new Compaction(null);
					comp.deserializeNBT(tag);
					return comp;
				})
				.forEach(comp -> map.put(comp.getID(), comp));
		;
	}

	@Override
	public ICompaction get(UUID id) {
		return map.get(id);
	}

	@Override
	@Nonnull
	public  ICompaction getOrElse(UUID id, Function<UUID, ? extends ICompaction> factory) {
		ICompaction compaction = get(id);
		if (compaction == null) {
			compaction = factory.apply(id);
			put(compaction);
		}
		return compaction;
	}

	@Override
	public void put(ICompaction comp) {
		map.put(comp.getID(), comp);
	}
}
