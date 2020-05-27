package white_blizz.ender_torment.common.tile_entity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.container.EnderFluxBatteryContainer;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.common.item.EnderFluxCapacitorItem;
import white_blizz.ender_torment.utils.CombinedWrapper;
import white_blizz.ender_torment.utils.IEnchantmentList;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxBatteryTE extends ETTileEntity implements INamedContainerProvider {
	@CapabilityInject(IItemHandler.class)
	public static Capability<IItemHandler> ITEM_HANDLER_CAP = null;
	@CapabilityInject(IEnderFluxStorage.class)
	public static Capability<IEnderFluxStorage> ENDER_FLUX_CAP = null;

	public EnderFluxBatteryTE() { super(ETBlocks.ENDER_FLUX_BATTERY_TYPE); }

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(ETBlocks.ENDER_FLUX_BATTERY.get().getTranslationKey());
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
		return new EnderFluxBatteryContainer(id, playerInventory, items);
	}

	private static class CombinedFluxCapacitors
			extends CombinedWrapper<IEnderFluxStorage, CombinedFluxCapacitors>
			implements IEnderFluxStorage {

		@Override protected CombinedFluxCapacitors me() { return this; }

		private CombinedFluxCapacitors(IEnderFluxStorage... caps) { super(caps); }

		@Override
		public int receiveEnderFlux(int maxReceive, boolean simulate) {
			return exe_I_IB_Sb(IEnderFluxStorage::receiveEnderFlux, maxReceive, simulate);
		}

		@Override
		public int extractEnderFlux(int maxExtract, boolean simulate) {
			return exe_I_IB_Sb(IEnderFluxStorage::extractEnderFlux, maxExtract, simulate);
		}

		@Override
		public int getMaxReceive() {
			return exe_I_Mx(IEnderFluxStorage::getMaxReceive, 0);
		}

		@Override
		public int getMaxExtract() {
			return exe_I_Mx(IEnderFluxStorage::getMaxExtract, 0);
		}

		@Override
		public int getEnderFluxStored() {
			return exe_I_Sm(IEnderFluxStorage::getEnderFluxStored);
		}

		@Override
		public int getMaxEnderFluxStored() {
			return exe_I_Sm(IEnderFluxStorage::getMaxEnderFluxStored);
		}

		@Override
		public double getDecayRate() {
			return exe_D_Av(IEnderFluxStorage::getDecayRate, 0D);
		}

		@Override
		public boolean canExtractFlux() {
			return exe_B_Or(IEnderFluxStorage::canExtractFlux, false);
		}

		@Override
		public boolean canReceiveFlux() {
			return exe_B_Or(IEnderFluxStorage::canReceiveFlux, false);
		}

		@Override
		public void setEnchantmentList(IEnchantmentList list) {
			exe_V_O(IEnderFluxStorage::setEnchantmentList, list);
		}

		@Override public CompoundNBT serializeNBT() { return new CompoundNBT(); }
		@Override public void deserializeNBT(CompoundNBT nbt) { }
	}

	private class Capacitors extends ItemStackHandler {
		private Capacitors() { super(4); }

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			return stack.getItem() instanceof EnderFluxCapacitorItem;
		}

		@Override
		protected void onContentsChanged(int slot) {
			markDirty();
		}
	}

	private final Capacitors items = new Capacitors();

	@Override
	protected void extraRead(CompoundNBT tag) {
		items.deserializeNBT(tag.getCompound("items"));
	}

	@Override
	protected void extraWrite(CompoundNBT tag) {
		tag.put("items", items.serializeNBT());
	}

	@Override
	protected List<Cap<?>> getCaps() {
		return CapList.New()
				.newCap(ITEM_HANDLER_CAP)
				.map(null, () -> items).next(ENDER_FLUX_CAP)
				.map(null, () -> CombinedWrapper.convert(
						CombinedFluxCapacitors::new,
						CombinedWrapper.fromItems(ENDER_FLUX_CAP, new IEnderFluxStorage[0]),
						items
				)).done()
				;
	}
}
