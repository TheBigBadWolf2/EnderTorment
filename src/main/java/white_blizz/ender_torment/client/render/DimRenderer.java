package white_blizz.ender_torment.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.client.IRenderHandler;
import white_blizz.ender_torment.common.dim.ETDim;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

public class DimRenderer {
	public static final DimRenderer INSTANCE = new DimRenderer();

	private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_TEXTURES = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_TEXTURES = new ResourceLocation("textures/misc/forcefield.png");
	private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");

	private final VertexFormat skyVertexFormat = DefaultVertexFormats.POSITION;

	@Nullable private VertexBuffer starVBO;
	@Nullable private VertexBuffer skyVBO;
	@Nullable private VertexBuffer sky2VBO;

	private DimRenderer() {
		generateStars();
		generateSky();
		generateSky2();
	}

	private void generateStars() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		if (this.starVBO != null) {
			this.starVBO.close();
		}

		this.starVBO = new VertexBuffer(this.skyVertexFormat);
		this.renderStars(bufferbuilder);
		bufferbuilder.finishDrawing();
		this.starVBO.upload(bufferbuilder);
	}

	private void renderStars(BufferBuilder bufferBuilderIn) {
		Random random = new Random(10842L);
		bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

		for(int i = 0; i < 1500; ++i) {
			double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
			double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
			double d4 = d0 * d0 + d1 * d1 + d2 * d2;
			if (d4 < 1.0D && d4 > 0.01D) {
				d4 = 1.0D / Math.sqrt(d4);
				d0 = d0 * d4;
				d1 = d1 * d4;
				d2 = d2 * d4;
				double d5 = d0 * 100.0D;
				double d6 = d1 * 100.0D;
				double d7 = d2 * 100.0D;
				double d8 = Math.atan2(d0, d2);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for(int j = 0; j < 4; ++j) {
					double d17 = 0.0D;
					double d18 = (double)((j & 2) - 1) * d3;
					double d19 = (double)((j + 1 & 2) - 1) * d3;
					double d20 = 0.0D;
					double d21 = d18 * d16 - d19 * d15;
					double d22 = d19 * d16 + d18 * d15;
					double d23 = d21 * d12 + 0.0D * d13;
					double d24 = 0.0D * d12 - d21 * d13;
					double d25 = d24 * d9 - d22 * d10;
					double d26 = d22 * d9 + d24 * d10;
					bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
				}
			}
		}

	}

	private void generateSky() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		if (this.skyVBO != null) {
			this.skyVBO.close();
		}

		this.skyVBO = new VertexBuffer(this.skyVertexFormat);
		this.renderSky(bufferbuilder, 16.0F, false);
		bufferbuilder.finishDrawing();
		this.skyVBO.upload(bufferbuilder);
	}

	private void generateSky2() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		if (this.sky2VBO != null) {
			this.sky2VBO.close();
		}

		this.sky2VBO = new VertexBuffer(this.skyVertexFormat);
		this.renderSky(bufferbuilder, -16.0F, true);
		bufferbuilder.finishDrawing();
		this.sky2VBO.upload(bufferbuilder);
	}

	private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
		int i = 64;
		int j = 6;
		bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

		for(int k = -384; k <= 384; k += 64) {
			for(int l = -384; l <= 384; l += 64) {
				float f = (float)k;
				float f1 = (float)(k + 64);
				if (reverseX) {
					f1 = (float)k;
					f = (float)(k + 64);
				}

				bufferBuilderIn.pos((double)f, (double)posY, (double)l).endVertex();
				bufferBuilderIn.pos((double)f1, (double)posY, (double)l).endVertex();
				bufferBuilderIn.pos((double)f1, (double)posY, (double)(l + 64)).endVertex();
				bufferBuilderIn.pos((double)f, (double)posY, (double)(l + 64)).endVertex();
			}
		}

	}


	private interface W<R> {
		R run(ClientWorld world, float partialTicks);
	}
	private interface D<R> {
		R run(ETDim dim, long time, float partialTicks);
	}

	private static class Runner {
		private final ClientWorld world;
		private final ETDim dim;
		private final float partialTicks;

		private Runner(ClientWorld world, float partialTicks) {
			this.world = world;
			Dimension dim =  world.dimension;
			if (dim instanceof ETDim) this.dim = (ETDim) dim;
			else this.dim = null;
			this.partialTicks = partialTicks;
		}

		<R> R run(W<R> clientRunner, D<R> dimRunner) {
			if (dim != null) return dimRunner.run(dim, world.getDayTime(), partialTicks);
			else return clientRunner.run(world, partialTicks);
		}
	}

	public void renderSky(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {
		// FIXME: TEMP (Copied from GameRenderer#renderWorld)
		final MatrixStack matrixStack = new MatrixStack();
		{
			ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
			net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(Minecraft.getInstance().gameRenderer, info, partialTicks);
			info.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
			matrixStack.rotate(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

			matrixStack.rotate(Vector3f.XP.rotationDegrees(info.getPitch()));
			matrixStack.rotate(Vector3f.YP.rotationDegrees(info.getYaw() + 180.0F));
		}
		renderSky(ticks, partialTicks, matrixStack, world, mc);
	}
	public void renderSky(int ticks, float partialTicks, MatrixStack stack, ClientWorld world, Minecraft mc) {
		assert starVBO != null;
		assert skyVBO != null;
		assert sky2VBO != null;

		ETDim dim = (ETDim)world.dimension;

		dim.updateCelestials(world.getDayTime(), partialTicks);

		//sky
		RenderSystem.disableTexture();
		Vec3d skyColor = world.getSkyColor(mc.gameRenderer.getActiveRenderInfo().getBlockPos(), partialTicks);
		float skyR = (float) skyColor.x;
		float skyG = (float) skyColor.y;
		float skyB = (float) skyColor.z;
		FogRenderer.applyFog();
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.depthMask(false);
		RenderSystem.enableFog();
		RenderSystem.color3f(skyR, skyG, skyB);
		this.skyVBO.bindBuffer();
		this.skyVertexFormat.setupBufferState(0L);
		this.skyVBO.draw(stack.getLast().getMatrix(), 7);
		VertexBuffer.unbindBuffer();
		this.skyVertexFormat.clearBufferState();
		RenderSystem.disableFog();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		//Sunset colors
		float[] sunColors = dim.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
		if (sunColors != null) {
			RenderSystem.disableTexture();
			RenderSystem.shadeModel(7425);
			stack.push();
			stack.rotate(Vector3f.XP.rotationDegrees(90.0F));
			float f3 = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F;
			stack.rotate(Vector3f.ZP.rotationDegrees(f3));
			stack.rotate(Vector3f.ZP.rotationDegrees(90.0F));
			float sunColorR = sunColors[0];
			float sunColorG = sunColors[1];
			float sunColorB = sunColors[2];
			Matrix4f matrix4f = stack.getLast().getMatrix();
			bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
			bufferbuilder.pos(matrix4f, 0.0F, 100.0F, 0.0F).color(sunColorR, sunColorG, sunColorB, sunColors[3]).endVertex();
			int i = 16;

			for (int j = 0; j <= 16; ++j) {
				float rad = (float) j * ((float) Math.PI * 2F) / 16.0F;
				float sin = MathHelper.sin(rad);
				float cos = MathHelper.cos(rad);
				bufferbuilder.pos(matrix4f, sin * 120.0F, cos * 120.0F, -cos * 40.0F * sunColors[3]).color(sunColors[0], sunColors[1], sunColors[2], 0.0F).endVertex();
			}

			bufferbuilder.finishDrawing();
			WorldVertexBufferUploader.draw(bufferbuilder);
			stack.pop();
			RenderSystem.shadeModel(7424);
		}


		RenderSystem.enableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		stack.push();
		//Rain
		float rain = 1.0F - world.getRainStrength(partialTicks);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, rain);
		//Suns
		mc.textureManager.bindTexture(SUN_TEXTURES);
		float depth = 100;
		for (ETDim.SunInfo sun : dim.getSuns()) {
			stack.push();
			RenderSystem.color4f(
					sun.getRed(),
					sun.getGreen(),
					sun.getBlue(),
					rain
			);
			stack.rotate(Vector3f.YP.rotationDegrees((sun.getMeridianAngle() * 360.0F) - 90.0F));
			stack.rotate(Vector3f.XP.rotationDegrees(sun.getNoonAngle() * 360.0F));
			Matrix4f matrix = stack.getLast().getMatrix();
			float size = sun.getSize();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(matrix, -size, depth, -size).tex(0.0F, 0.0F).endVertex();
			bufferbuilder.pos(matrix, size, depth, -size).tex(1.0F, 0.0F).endVertex();
			bufferbuilder.pos(matrix, size, depth, size).tex(1.0F, 1.0F).endVertex();
			bufferbuilder.pos(matrix, -size, depth, size).tex(0.0F, 1.0F).endVertex();
			bufferbuilder.finishDrawing();
			WorldVertexBufferUploader.draw(bufferbuilder);
			stack.pop();
		}

		//Moons
		mc.textureManager.bindTexture(MOON_PHASES_TEXTURES);
		depth = -100;
		for (ETDim.MoonInfo moon : dim.getMoons()) {
			stack.push();
			RenderSystem.color4f(
					moon.getRed(),
					moon.getGreen(),
					moon.getBlue(),
					rain
			);
			stack.rotate(Vector3f.YP.rotationDegrees((moon.getMeridianAngle() * 360.0F) - 90.0F));
			stack.rotate(Vector3f.XP.rotationDegrees((moon.getNoonAngle() * 360.0F)));
			Matrix4f matrix = stack.getLast().getMatrix();
			float size = moon.getSize();

			int moonPhase = moon.getPhase();
			int moonU = moonPhase % 4;
			int moonV = moonPhase / 4 % 2;
			float moonU1 = (float) (moonU) / 4.0F;
			float moonV1 = (float) (moonV) / 2.0F;
			float moonU2 = (float) (moonU + 1) / 4.0F;
			float moonV2 = (float) (moonV + 1) / 2.0F;
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(matrix, -size, depth, size).tex(moonU2, moonV2).endVertex();
			bufferbuilder.pos(matrix, size, depth, size).tex(moonU1, moonV2).endVertex();
			bufferbuilder.pos(matrix, size, depth, -size).tex(moonU1, moonV1).endVertex();
			bufferbuilder.pos(matrix, -size, depth, -size).tex(moonU2, moonV1).endVertex();
			bufferbuilder.finishDrawing();
			WorldVertexBufferUploader.draw(bufferbuilder);
			RenderSystem.disableTexture();
			stack.pop();
		}

		//Stars
		for (ETDim.StarInfo star : dim.getStars()) {
			stack.push();
			stack.rotate(Vector3f.YP.rotationDegrees((star.getMeridianAngle() * 360.0F) - 90.0F));
			stack.rotate(Vector3f.XP.rotationDegrees((star.getNoonAngle() * 360.0F)));
			float starLight = star.getBrightness() * rain;
			if (starLight > 0.0F) {
				RenderSystem.color4f(
						starLight * star.getRed(),
						starLight * star.getGreen(),
						starLight * star.getBlue(),
						starLight
				);
				this.starVBO.bindBuffer();
				this.skyVertexFormat.setupBufferState(0L);
				this.starVBO.draw(stack.getLast().getMatrix(), 7);
				VertexBuffer.unbindBuffer();
				this.skyVertexFormat.clearBufferState();
			}
			stack.pop();
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableFog();
		stack.pop();
		RenderSystem.disableTexture();
		RenderSystem.color3f(0.0F, 0.0F, 0.0F);
		//Horizon
		double horizon = mc.player.getEyePosition(partialTicks).y - world.getHorizonHeight();
		if (horizon < 0.0D) {
			stack.push();
			stack.translate(0.0D, 12.0D, 0.0D);
			this.sky2VBO.bindBuffer();
			this.skyVertexFormat.setupBufferState(0L);
			this.sky2VBO.draw(stack.getLast().getMatrix(), 7);
			VertexBuffer.unbindBuffer();
			this.skyVertexFormat.clearBufferState();
			stack.pop();
		}

		//Sky
		if (dim.isSkyColored()) {
			RenderSystem.color3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			RenderSystem.color3f(skyR, skyG, skyB);
		}

		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
		RenderSystem.disableFog();
	}

	public void renderWeather(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {}
	public void renderClouds(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {}
}
