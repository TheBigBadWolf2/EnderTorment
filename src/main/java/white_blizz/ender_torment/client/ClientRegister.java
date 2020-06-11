package white_blizz.ender_torment.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.client.render.CompactionRenderer;
import white_blizz.ender_torment.client.render.ConduitRenderer;
import white_blizz.ender_torment.client.render.ETLightMap;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Predicate;
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = Ref.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegister {
	public static final SnowModel.Loader SNOW_LOADER = new SnowModel.Loader();

	@SubscribeEvent public static void setup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(ETBlocks.CONDUIT_TYPE.get(), ConduitRenderer::new);
		if (ETBlocks.COMPACTION_TYPE != null)
			ClientRegistry.bindTileEntityRenderer(ETBlocks.COMPACTION_TYPE.get(), CompactionRenderer::new);
		DeferredWorkQueue.runLater(ClientRegister::swap);
		ModelLoaderRegistry.registerLoader(Ref.MOD.rl.loc("snow"), SNOW_LOADER);

		Predicate<RenderType> test = type -> type == RenderType.getTranslucent();
		RenderTypeLookup.setRenderLayer(ETBlocks.SNOW_BLOCK.get(), test);
		RenderTypeLookup.setRenderLayer(ETBlocks.ICE.get(), test);
		RenderTypeLookup.setRenderLayer(ETBlocks.PACKED_ICE.get(), test);
		RenderTypeLookup.setRenderLayer(ETBlocks.BLUE_ICE.get(), test);
	}

	public static void addPackFinder() {
		Minecraft.getInstance().getResourcePackList()
				.addPackFinder(new IPackFinder() {
					@Override
					public <T extends ResourcePackInfo> void addPackInfosToMap(
							Map<String, T> nameToPackMap,
							ResourcePackInfo.IFactory<T> packInfoFactory) {
						Pack pack = Pack.INSTANCE;
						nameToPackMap.put(pack.getName(),
								packInfoFactory.create(pack.getName(),
										true, () -> pack, pack,
										new PackMetadataSection(new StringTextComponent("Ender Torment Fading Textures"), 5),
										ResourcePackInfo.Priority.TOP
								)
						);
					}
				});
	}

	private static void swap() {
		Minecraft mc = Minecraft.getInstance();
		GameRenderer renderer = mc.gameRenderer;
		Field field_lightmapTexture = ObfuscationReflectionHelper.findField(
				GameRenderer.class,
				"field_78513_d"
		);

		Field field_modifiers = ObfuscationReflectionHelper.findField(Field.class, "modifiers");
		try {
			int modifiers = field_modifiers.getInt(field_lightmapTexture);
			modifiers &= ~Modifier.FINAL;
			field_modifiers.setInt(field_lightmapTexture, modifiers);
			field_lightmapTexture.set(renderer, new ETLightMap(renderer, mc));
		}
		catch (IllegalAccessException ignored) {}
	}
}