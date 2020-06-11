package white_blizz.ender_torment.common.dim;

import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

public final class ETDims extends ETDeferredRegisterHandler {
	static final String DARK_NAME = "dark_shadow";

	private static final DeferredRegister<ModDimension> DIM = New(ForgeRegistries.MOD_DIMENSIONS);
	private static final DeferredRegister<BiomeProviderType<?, ?>> BIOME = New(ForgeRegistries.BIOME_PROVIDER_TYPES);
	private static final DeferredRegister<ChunkGeneratorType<?, ?>> CHUNK = New(ForgeRegistries.CHUNK_GENERATOR_TYPES);

	public static final RegistryObject<ETModDim> DARK_DIM = DIM.register(DARK_NAME, ETModDim::new);
	public static final RegistryObject<BiomeProviderType<ETBiomeProvider.Settings, ETBiomeProvider>> DARK_BIOME_PROVIDER = BIOME.register(DARK_NAME+"_biome_provider", () -> new BiomeProviderType<>(ETBiomeProvider::new, ETBiomeProvider.Settings::new));
	public static final RegistryObject<ChunkGeneratorType<ETChunkGen.Config, ETChunkGen>> DARK_CHUNK_GEN = CHUNK.register(DARK_NAME+"_chunk_gen", () -> new ChunkGeneratorType<>(ETChunkGen::new, false, ETChunkGen.Config::new));

	public static DimensionType DARK_TYPE;

	@SubscribeEvent
	public void onDimensionRegistry(RegisterDimensionsEvent event) {
		DARK_TYPE = DimensionManager.registerOrGetDimension(
				DARK_DIM.getId(),
				DARK_DIM.get(),
				null,
				true
		);
	}

	@SubscribeEvent
	public void onServerInit(FMLServerAboutToStartEvent evt) {
		DARK_TYPE = DimensionManager.registerOrGetDimension(
				DARK_DIM.getId(),
				DARK_DIM.get(),
				null,
				true
		);
	}

	public ETDims(IEventBus bus) { super(bus, DIM, BIOME, CHUNK); }

	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return true; }
}
