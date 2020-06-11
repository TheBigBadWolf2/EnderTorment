package white_blizz.ender_torment.common.dim;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

public final class ETBiomes extends ETDeferredRegisterHandler {
	private static final DeferredRegister<Biome> BIOME = New(ForgeRegistries.BIOMES);
	private static final DeferredRegister<SurfaceBuilder<?>> SURFACE = New(ForgeRegistries.SURFACE_BUILDERS);

	static final ETSurfaceBuilder BUILDER = new ETSurfaceBuilder(); //Since biomes run first and need the builder...
	public static final RegistryObject<ETSurfaceBuilder> DARK_BUILDER = SURFACE.register(ETDims.DARK_NAME+"_builder", () -> BUILDER);
	public static final RegistryObject<ETBiome> DARK = BIOME.register(ETDims.DARK_NAME+"_biome", ETBiome::new);

	public ETBiomes(IEventBus bus) { super(bus, BIOME, SURFACE); }
	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return false; }
}
