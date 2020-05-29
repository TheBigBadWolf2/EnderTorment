package white_blizz.ender_torment.common.conduit;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import white_blizz.ender_torment.common.conduit.io.IConduitBuffer;
import white_blizz.ender_torment.common.conduit.io.IConduitInput;
import white_blizz.ender_torment.common.conduit.io.IConduitOutput;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ConduitTypes {
	static final Random RNG = new Random();

	public static final ConduitType<IEnergyStorage> ENERGY = new ConduitType<>(
			0xFFFF0000,
			IEnergyStorage.class,
			() -> CapabilityEnergy.ENERGY,
			null).setRegName("energy");
	public static final ConduitType<IItemHandler> ITEM = new ConduitType<>(
			0xFFFFFFFF,
			IItemHandler.class,
			() -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
			new ItemConduitTypeHandler()).setRegName("item");
	public static final ConduitType<IFluidHandler> FLUID = new ConduitType<>(
			0xFF0000FF,
			IFluidHandler.class,
			() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
			null).setRegName("fluid");

	public static final ConduitType<IEnderFluxStorage> ENDER_FLUX = new ConduitType<>(
			0xFFFF00FF,
			IEnderFluxStorage.class,
			() -> CapabilityEnderFlux.ENDER_FLUX,
			null).setRegName("ender_flux");

	@SubscribeEvent
	public static void register(RegistryEvent.Register<ConduitType<?>> evt) {
		evt.getRegistry().registerAll(
				ENERGY,
				ITEM,
				FLUID,
				ENDER_FLUX
		);
	}
}
