package white_blizz.ender_torment.common.container;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import white_blizz.ender_torment.client.gui.EnderFluxBatteryScreen;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

public final class ETContainers extends ETDeferredRegisterHandler {
	private static final DeferredRegister<ContainerType<?>> CONTAINERS = New(ForgeRegistries.CONTAINERS);

	public static final RegistryObject<ContainerType<EnderFluxBatteryContainer>> ENDER_FLUX_BATTERY = CONTAINERS.register(
			"ender_flux_battery", () -> IForgeContainerType.create(EnderFluxBatteryContainer::new));

	public ETContainers(IEventBus bus) { super(bus, CONTAINERS); }

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void regScreens(FMLClientSetupEvent evt) {
		DeferredWorkQueue.runLater(() -> {
			ScreenManager.registerFactory(ENDER_FLUX_BATTERY.get(), EnderFluxBatteryScreen::new);
		});
	}

	@Override public boolean regModBus() { return true; }
	@Override public boolean regForgeBus() { return false; }
}
