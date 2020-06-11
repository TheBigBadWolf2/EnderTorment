package white_blizz.ender_torment.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.utils.Ref;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Ref.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandlerMain {

	@SubscribeEvent
	public static void mouseWheel(InputEvent.MouseScrollEvent evt) {

	}

	@SubscribeEvent
	public static void texture(TextureStitchEvent.Pre evt) {
		if (evt.getMap().getTextureLocation().equals(SnowModel.LOCATION_BLOCKS_TEXTURE))
			Pack.INSTANCE.getNames().forEach(evt::addSprite);
	}
}
