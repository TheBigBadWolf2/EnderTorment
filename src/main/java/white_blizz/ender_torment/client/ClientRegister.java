package white_blizz.ender_torment.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import white_blizz.ender_torment.client.render.ConduitRenderer;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.utils.Ref;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegister {
	@SubscribeEvent public static void setup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(ETBlocks.CONDUIT_TYPE.get(), ConduitRenderer::new);
	}
}