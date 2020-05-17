package white_blizz.ender_torment.common.ender_flux;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public final class CapabilityEnderFlux {
	@CapabilityInject(IEnderFluxStorage.class)
	public static Capability<IEnderFluxStorage> ENDER_FLUX;

	public static void register() {
		Storage storage = new Storage();
		CapabilityManager.INSTANCE.register(
				IEnderFluxStorage.class,
				storage, storage
		);
	}

	private static class Storage implements
			Capability.IStorage<IEnderFluxStorage>,
			Callable<IEnderFluxStorage> {

		@Nullable
		@Override
		public INBT writeNBT(Capability<IEnderFluxStorage> capability, IEnderFluxStorage instance, Direction side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<IEnderFluxStorage> capability, IEnderFluxStorage instance, Direction side, INBT nbt) {
			instance.deserializeNBT((CompoundNBT) nbt);
		}

		@Override
		public IEnderFluxStorage call() throws Exception {
			return new EnderFluxStorage(1000);
		}
	}
}
