package white_blizz.ender_torment.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderHelper {

	@SubscribeEvent
	public static void preRender(TickEvent.RenderTickEvent evt) {
		if (evt.phase == TickEvent.Phase.START) {
			ClientWorld world = Minecraft.getInstance().world;
			final float time;
			if (world != null) time = world.getGameTime() + evt.renderTickTime;
			else time = 0;
			ConduitRenderer.TIME = time;
			ConduitRenderer.MAP.values().forEach(model -> model.update(time));
		}
	}
}
