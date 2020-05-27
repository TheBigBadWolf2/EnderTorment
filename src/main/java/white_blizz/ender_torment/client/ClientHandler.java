package white_blizz.ender_torment.client;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.Ref;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Ref.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientHandler {
	private static final ResourceLocation EMPTY_SHADER_LOC = Ref.MOD.rl.loc(
			"shaders/post/",
			"empty",
			"json"
	);
	private static final ResourceLocation SHADER_LOC = Ref.MOD.rl.loc(
			"shaders/post/",
			"flux_vision",
			"json"
	);


	private static ShaderGroup shaderGroup;
	//private static Shader combiner;

	private static void newGroup() throws IOException {
		if (shaderGroup != null) shaderGroup.close();
		//if (combiner != null) combiner.close();

		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();
		shaderGroup = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				SHADER_LOC
		);
		//shaderGroup.addFramebuffer(Ref.locStr("main"), w, h);
		shaderGroup.createBindFramebuffers(w, h);


		/*Framebuffer a = shaderGroup.getFramebufferRaw("a");
		Framebuffer b = shaderGroup.getFramebufferRaw("b");

		combiner = new Shader(
				mc.getResourceManager(),
				Ref.locStr("combine"),
				a, mc.getFramebuffer()
		);
		combiner.addAuxFramebuffer("Outlines", b, w, h);*/
	}

	private static void newGroup2() throws IOException {
		if (shaderGroup != null) shaderGroup.close();
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();
		shaderGroup = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				SHADER_LOC
		);
		shaderGroup.createBindFramebuffers(w, h);
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

	private static void doSpecialRender(String name, BiConsumer<Minecraft, ClientWorld> renderer) {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		if (world == null || shaderGroup == null) return;
		Framebuffer framebuffer = shaderGroup.getFramebufferRaw(name);
		//100% can be null
		//noinspection ConstantConditions
		if (framebuffer == null) return;
		framebuffer.bindFramebuffer(true);
		renderer.accept(mc, world);
		framebuffer.unbindFramebuffer();
	}

	private static void specialRenders(MatrixStack matrixStack, float partialTicks) {
		doSpecialRender(Ref.MOD.str.loc("main"), (mc, world) -> {
			Vec3d cam = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
			double cX = cam.getX();
			double cY = cam.getY();
			double cZ = cam.getZ();

			for (Entity entity : world.getAllEntities()) {
				if (entity instanceof PlayerEntity) continue;

				double eX = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX());
				double eY = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY());
				double eZ = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ());
				float f = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw);
				mc.getRenderManager().renderEntityStatic(
						entity,
						eX - cX, eY - cY, eZ - cZ, f,
						partialTicks,
						matrixStack,
						mc.getRenderTypeBuffers().getBufferSource(),
						mc.getRenderManager().getPackedLight(entity, partialTicks)
				);
			}
		});
	}

	public static void renderA(RenderWorldLastEvent event) throws IOException {
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();


		ClientPlayerEntity player = mc.player;
		if (player == null) return;
		if (player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			float partialTicks = mc.getRenderPartialTicks();

			if (shaderGroup == null) newGroup();

			shaderGroup.createBindFramebuffers(w, h);

			specialRenders(event.getMatrixStack(), partialTicks);
			render(partialTicks);


			//double depth = 500;



			/*RenderSystem.pushMatrix();
			RenderSystem.ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
			mc.getFramebuffer().bindFramebuffer(true);
			frame.bindFramebufferTexture();
			RenderSystem.depthMask(false);
			BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos(0.0D, 0.0D, depth).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(w, 0.0D, depth).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(w, h, depth).color(255, 255, 255, 255).endVertex();
			bufferbuilder.pos(0.0D, h, depth).color(255, 255, 255, 255).endVertex();
			bufferbuilder.finishDrawing();
			WorldVertexBufferUploader.draw(bufferbuilder);
			RenderSystem.depthMask(true);
			mc.getFramebuffer().unbindFramebufferTexture();
			frame.unbindFramebufferTexture();
			RenderSystem.popMatrix();*/
		} else {
			if (shaderGroup != null){
				shaderGroup.close();
				shaderGroup = null;
			}
			/*if (combiner != null) {
				combiner.close();
				combiner = null;
			}*/
		}
	}

	private static void render(float partialTicks) {
		//RenderSystem.pushMatrix();
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.matrixMode(5890);
		//RenderSystem.pushMatrix(); //Overflow?
		RenderSystem.loadIdentity();
		shaderGroup.render(partialTicks);
		//RenderSystem.popMatrix(); //Underflow?
		RenderSystem.matrixMode(5889);
		//RenderSystem.popMatrix(); //Underflow?
	}
}
