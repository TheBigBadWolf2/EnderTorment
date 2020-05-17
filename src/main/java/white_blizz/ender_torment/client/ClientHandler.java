package white_blizz.ender_torment.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.Ref;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Ref.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandler {
	private static final ResourceLocation SHADER_LOC = Ref.loc(
			"shaders/post/",
			"flux_vision",
			"json"
	);


	private static ShaderGroup shaderGroup;

	private static ShaderGroup newGroup() throws IOException {
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();
		ShaderGroup shaderGroup = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				SHADER_LOC
		);
		//shaderGroup.addFramebuffer(Ref.locStr("main"), w, h);
		shaderGroup.createBindFramebuffers(w, h);

		return shaderGroup;
	}

	/*@SubscribeEvent
	public static void render(EntityViewRenderEvent.CameraSetup event) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null) return;
		GameRenderer renderer = event.getRenderer();
		ShaderGroup shaderGroup = renderer.getShaderGroup();
		if (shaderGroup == null && player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			renderer.loadShader(SHADER_LOC);
			*//*shaderGroup = renderer.getShaderGroup();
			if (shaderGroup != null) {
				Framebuffer in = shaderGroup.getFramebufferRaw("in");
				Framebuffer out = shaderGroup.getFramebufferRaw("out");
				try {
					Shader shader = shaderGroup.addShader(Ref.locStr("test"), in, out);
					int w = Minecraft.getInstance().getMainWindow().getFramebufferWidth();
					int h = Minecraft.getInstance().getMainWindow().getFramebufferHeight();
					Framebuffer frame = new Framebuffer(w, h, true, false);
					renBuf = GlStateManager.genRenderbuffers();
					GlStateManager.framebufferRenderbuffer(FramebufferConstants.GL_FRAMEBUFFER, FramebufferConstants.GL_COLOR_ATTACHMENT0, FramebufferConstants.GL_RENDERBUFFER, renBuf);
					GlStateManager.renderbufferStorage(renBuf, GL11.GL_RGBA, w, h);
					GlStateManager.bindRenderbuffer(FramebufferConstants.GL_RENDERBUFFER, renBuf);

					shader.addAuxFramebuffer(
							"Test",
							frame,
							w, h
					);
				} catch (IOException e) { e.printStackTrace(); }
			}*//*
		} else if (shaderGroup != null
				&& shaderGroup.getShaderGroupName().equals(SHADER_LOC.toString())
				&& !player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			if (renBuf != -1) {
				GlStateManager.deleteRenderbuffers(renBuf);
				renBuf = -1;
			}
			renderer.stopUseShader();
		}


		*//*if (set != null) {
			if (set) {
				event.getRenderer().loadShader(
						Ref.loc(
								"shaders/post/",
								"flux_vision",
								"json"
						));
			}
			else event.getRenderer().stopUseShader();
			set = null;
		}*//*
		*//*ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null) return;
		if (player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			event.getRenderer().loadShader(new ResourceLocation("shaders/post/pencil.json"));
		}*//*
	}*/

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();

		/*if (mc.world != null) {
			for (Entity entity : mc.world.getAllEntities()) {
				EntityRenderer<? super Entity> renderer = mc.getRenderManager().getRenderer(entity);

			}
		}*/

		ClientPlayerEntity player = mc.player;
		if (player == null) return;
		if (player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			float partialTicks = mc.getRenderPartialTicks();

			if (shaderGroup == null) {
				try { shaderGroup = newGroup(); }
				catch (IOException e) { e.printStackTrace(); }
			}
			/*Framebuffer framebuffer = shaderGroup.getFramebufferRaw(Ref.locStr("main"));
			framebuffer.bindFramebuffer(true);

			if (mc.world != null) {
				for (Entity entity : mc.world.getAllEntities()) {
					EntityRenderer<? super Entity> renderer = mc.getRenderManager().getRenderer(entity);
					renderer.render(entity, entity.rotationYaw, event.getPartialTicks(),
							event.getMatrixStack(), mc.getRenderTypeBuffers().getBufferSource(),
							mc.getRenderManager().getPackedLight(entity, event.getPartialTicks())
					);
				}
			}

			framebuffer.unbindFramebuffer();*/

			shaderGroup.render(partialTicks);

			Framebuffer a = shaderGroup.getFramebufferRaw("a");
			a.bindFramebufferTexture();
			RenderSystem.depthMask(false);
			BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos(0.0D, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(w, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(w, h, 500.0D).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(0.0D, h, 500.0D).color(255, 255, 255, 255).endVertex();
			bufferbuilder.finishDrawing();
			WorldVertexBufferUploader.draw(bufferbuilder);
			RenderSystem.depthMask(true);
		}
	}
}
