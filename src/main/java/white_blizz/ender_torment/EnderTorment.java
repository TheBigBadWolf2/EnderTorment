package white_blizz.ender_torment;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import white_blizz.ender_torment.client.ClientConfig;
import white_blizz.ender_torment.client.ClientRegister;
import white_blizz.ender_torment.common.CommonConfig;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.command.ETCommands;
import white_blizz.ender_torment.common.compaction.CapabilityCompaction;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.conduit.ConduitTypes;
import white_blizz.ender_torment.common.conduit.Network;
import white_blizz.ender_torment.common.container.ETContainers;
import white_blizz.ender_torment.common.dim.ETBiomes;
import white_blizz.ender_torment.common.dim.ETDims;
import white_blizz.ender_torment.common.enchantment.CapabilityEnchantableBlock;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.item.ETItems;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.intergration.top.TOPHandler;
import white_blizz.ender_torment.server.ServerConfig;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;
import white_blizz.ender_torment.utils.IConfig;
import white_blizz.ender_torment.utils.ISidedConfig;
import white_blizz.ender_torment.utils.Ref;

import java.util.Arrays;

@Mod(Ref.MOD_ID)
public final class EnderTorment {
	//Todo: Add mini boss named "Lambda"

	private static EnderTorment INSTANCE;
	public static EnderTorment getINSTANCE() { return INSTANCE; }

	public final ISidedConfig SIDED_CONFIG;
	public final IConfig COMMON_CONFIG;

	public static final Logger LOGGER = LogManager.getLogger();

	@FunctionalInterface
	public interface IETDeferredRegisterHandlerFactory {
		ETDeferredRegisterHandler make(IEventBus bus);
	}

	public static void registerDeferredRegisterHandlers(
			IETDeferredRegisterHandlerFactory... factories
	) {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		Arrays.stream(factories)
				.map(factory -> factory.make(modEventBus))
				.forEach(handler -> {
					if (handler.regModBus()) modEventBus.register(handler);
					if (handler.regForgeBus()) forgeEventBus.register(handler);
				});
	}

	public EnderTorment() {
		INSTANCE = this;
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		modEventBus.addListener(this::preInit);
		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(this::enqueueMSGs);
		modEventBus.addListener(ETRegistry::register);
		modEventBus.addGenericListener(ConduitType.class, ConduitTypes::register);

		forgeEventBus.addListener(this::commands);
		forgeEventBus.register(Network.class);

		registerDeferredRegisterHandlers(
				ETItems::new,
				ETBlocks::new,
				ETEnchantments::new,
				ETContainers::new,
				ETEffects::new,
				ETDims::new,
				ETBiomes::new
		);

		SIDED_CONFIG = DistExecutor.safeRunForDist(() -> ClientConfig::new, () -> ServerConfig::new);
		COMMON_CONFIG = new CommonConfig();

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientRegister::addPackFinder);
	}


	private void gatherData(GatherDataEvent event) {
		EnderTormentData.gatherData(event);
	}

	private void preInit(FMLCommonSetupEvent evt) {
		CapabilityEnderFlux.register();
		CapabilityCompaction.register();
		CapabilityEnchantableBlock.register();
	}

	private void commands(FMLServerStartingEvent evt) {
		ETCommands.register(evt.getCommandDispatcher());
	}

	private void enqueueMSGs(InterModEnqueueEvent evt) {
		InterModComms.sendTo("theoneprobe", "getTheOneProbe",
				TOPHandler.GetTheOneProbe::new);
	}
}
