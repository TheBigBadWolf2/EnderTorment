package white_blizz.ender_torment.common.conduit;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;

public class ConduitTypes {
	public static final ConduitType<IEnergyStorage> ENERGY = new ConduitType<>(
			0xFFFF0000,
			IEnergyStorage.class,
			() -> CapabilityEnergy.ENERGY, "energy"
	);
	public static final ConduitType<IItemHandler> ITEM = new ConduitType<>(
			0xFFFFFFFF,
			IItemHandler.class,
			() -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, "item"
	);
	public static final ConduitType<IFluidHandler> FLUID = new ConduitType<>(
			0xFF0000FF,
			IFluidHandler.class,
			() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, "fluid"
	);

	public static final ConduitType<IEnderFluxStorage> ENDER_FLUX = new ConduitType<>(
			0xFFFF00FF,
			IEnderFluxStorage.class,
			() -> CapabilityEnderFlux.ENDER_FLUX, "ender_flux"
	);

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
