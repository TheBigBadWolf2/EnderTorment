package white_blizz.ender_torment.common.compaction;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapabilityCompaction {
	@CapabilityInject(ICompaction.class)
	public static Capability<ICompaction> COMPACTION;
	@CapabilityInject(IWorldCompaction.class)
	public static Capability<IWorldCompaction> WORLD_COMPACTION;

	public static void register() {
		register(new CompactionStorage());
		register(new WorldCompactionStorage());
	}

	private static <T> void register(Storage<T> storage) {
		CapabilityManager.INSTANCE.register(
				storage.type,
				storage, storage
		);
	}

	private static class CompactionStorage extends Storage<ICompaction> {
		private CompactionStorage() { super(ICompaction.class); }
		@Override public ICompaction call() { return new Compaction(null); }

		@Nullable
		@Override
		public INBT writeNBT(Capability<ICompaction> capability, ICompaction instance, Direction side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<ICompaction> capability, ICompaction instance, Direction side, INBT nbt) {
			instance.deserializeNBT((CompoundNBT) nbt);
		}
	}

	private static class WorldCompactionStorage extends Storage<IWorldCompaction> {
		private WorldCompactionStorage() { super(IWorldCompaction.class); }
		@Override public IWorldCompaction call() { return new WorldCompaction(); }

		@Nullable
		@Override
		public INBT writeNBT(Capability<IWorldCompaction> capability, IWorldCompaction instance, Direction side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<IWorldCompaction> capability, IWorldCompaction instance, Direction side, INBT nbt) {
			instance.deserializeNBT((ListNBT) nbt);
		}
	}

	private static abstract class Storage<T> implements
			Capability.IStorage<T>,
			Callable<T> {
		private final Class<T> type;
		protected Storage(Class<T> type) { this.type = type; }
	}
}