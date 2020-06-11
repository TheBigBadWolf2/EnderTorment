package white_blizz.ender_torment.common.compaction;

import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Function;

public interface IWorldCompaction extends INBTSerializable<ListNBT> {
	ICompaction get(UUID id);
	@Nonnull ICompaction getOrElse(UUID id, Function<UUID, ? extends ICompaction> factory);
	void put(ICompaction compaction);
}