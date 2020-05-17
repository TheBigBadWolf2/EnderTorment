package white_blizz.ender_torment.common.enchantment;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public final class CapabilityEnchantableBlock {
	@CapabilityInject(IEnchantableBlock.class)
	public static Capability<IEnchantableBlock> ENCHANTABLE_BLOCK;

	public static void register() {
		Storage storage = new Storage();
		CapabilityManager.INSTANCE.register(
				IEnchantableBlock.class,
				storage, storage
		);
	}

	private static class Storage implements
			Capability.IStorage<IEnchantableBlock>,
			Callable<IEnchantableBlock> {

		@Override
		public IEnchantableBlock call() {
			return new EnchantableBlock(new ListNBT());
		}

		@Nullable @Override
		public INBT writeNBT(Capability<IEnchantableBlock> capability, IEnchantableBlock instance, Direction side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<IEnchantableBlock> capability, IEnchantableBlock instance, Direction side, INBT nbt) {
			instance.deserializeNBT((ListNBT) nbt);
		}
	}
}