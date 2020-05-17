package white_blizz.ender_torment;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.container.ETContainers;
import white_blizz.ender_torment.common.enchantment.CapabilityEnchantableBlock;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.intergration.top.TOPHandler;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;
import white_blizz.ender_torment.utils.Ref;

import java.util.List;

@Mod(Ref.MOD_ID)
public final class EnderTorment {
	//Todo: Add mini boss named "Lambda"

	public static final Logger LOGGER = LogManager.getLogger();

	public EnderTorment() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		modEventBus.addListener(this::preInit);
		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(this::enqueueMSGs);

		List<ETDeferredRegisterHandler> handlers = ImmutableList.of(
				new ETItems(modEventBus),
				new ETBlocks(modEventBus),
				new ETEnchantments(modEventBus),
				new ETContainers(modEventBus),
				new ETEffects(modEventBus)
		);

		for (ETDeferredRegisterHandler handler : handlers) {
			if (handler.regModBus()) modEventBus.register(handler);
			if (handler.regForgeBus()) forgeEventBus.register(handler);
		}
	}

	private void gatherData(GatherDataEvent event) {
		EnderTormentData.gatherData(event);
	}

	private void preInit(FMLCommonSetupEvent evt) {
		CapabilityEnderFlux.register();
		CapabilityEnchantableBlock.register();
	}

	private void enqueueMSGs(InterModEnqueueEvent evt) {
		InterModComms.sendTo("theoneprobe", "getTheOneProbe",
				TOPHandler.GetTheOneProbe::new);
	}
}
