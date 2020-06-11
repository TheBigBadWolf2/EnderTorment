package white_blizz.ender_torment.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import white_blizz.ender_torment.common.dim.ETDim;
import white_blizz.ender_torment.common.dim.ETDims;

public class ETLightMap extends LightTexture {
	private final DynamicTexture dynamicTexture;
	private final NativeImage nativeImage;
	private final ResourceLocation resourceLocation;
	private boolean needsUpdate;
	private float torchFlicker;
	private final GameRenderer entityRenderer;
	private final Minecraft client;

	public ETLightMap(GameRenderer renderer, Minecraft mc) {
		super(renderer, mc);
		super.close();
		this.entityRenderer = renderer;
		this.client = mc;
		this.dynamicTexture = new DynamicTexture(16, 16, false);
		this.resourceLocation = this.client.getTextureManager().getDynamicTextureLocation("light_map", this.dynamicTexture);
		this.nativeImage = this.dynamicTexture.getTextureData();

		for(int i = 0; i < 16; ++i) {
			for(int j = 0; j < 16; ++j) {
				this.nativeImage.setPixelRGBA(j, i, -1);
			}
		}

		this.dynamicTexture.updateDynamicTexture();
	}

	public void close() {
		this.dynamicTexture.close();
	}

	public void tick() {
		this.torchFlicker = (float)((double)this.torchFlicker + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
		this.torchFlicker = (float)((double)this.torchFlicker * 0.9D);
		this.needsUpdate = true;
	}

	public void disableLightmap() {
		RenderSystem.activeTexture(33986);
		RenderSystem.disableTexture();
		RenderSystem.activeTexture(33984);
	}

	public void enableLightmap() {
		RenderSystem.activeTexture(33986);
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		float f = 0.00390625F;
		RenderSystem.scalef(0.00390625F, 0.00390625F, 0.00390625F);
		RenderSystem.translatef(8.0F, 8.0F, 8.0F);
		RenderSystem.matrixMode(5888);
		this.client.getTextureManager().bindTexture(this.resourceLocation);
		RenderSystem.texParameter(3553, 10241, 9729);
		RenderSystem.texParameter(3553, 10240, 9729);
		RenderSystem.texParameter(3553, 10242, 10496);
		RenderSystem.texParameter(3553, 10243, 10496);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
		RenderSystem.activeTexture(33984);
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

	public void updateLightmap(float partialTicks) {
		if (this.needsUpdate) {
			this.needsUpdate = false;
			this.client.getProfiler().startSection("lightTex");
			ClientWorld clientworld = this.client.world;
			if (clientworld != null) {
				Dimension dim = clientworld.dimension;

				Runner runner = new Runner(clientworld, partialTicks);

				float sun = runner.run(ClientWorld::getSunBrightness, ETDim::getSunBrightness);//clientworld.getSunBrightness(partialTicks);
				float sun1;
				if (clientworld.getTimeLightningFlash() > 0) {
					sun1 = 1.0F;
				} else {
					sun1 = sun * 0.95F + 0.05F;
				}

				float waterBrightness = this.client.player.getWaterBrightness();
				float brightness;
				if (this.client.player.isPotionActive(Effects.NIGHT_VISION)) {
					brightness = GameRenderer.getNightVisionBrightness(this.client.player, partialTicks);
				} else if (waterBrightness > 0.0F && this.client.player.isPotionActive(Effects.CONDUIT_POWER)) {
					brightness = waterBrightness;
				} else {
					brightness = 0.0F;
				}

				Vector3f vector3f = new Vector3f(sun, sun, 1.0F);
				vector3f.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
				float f4 = this.torchFlicker + 1.5F;
				Vector3f lightColor = new Vector3f();

				for(int i = 0; i < 16; ++i) {
					for(int j = 0; j < 16; ++j) {
						float skyLight = this.getLightBrightness(clientworld, i) * sun1;
						float blockLight = this.getLightBrightness(clientworld, j) * f4;
						float f7 = blockLight * ((blockLight * 0.6F + 0.4F) * 0.6F + 0.4F);
						float f8 = blockLight * (blockLight * blockLight * 0.6F + 0.4F);
						lightColor.set(blockLight, f7, f8);
						if (dim.getType() == DimensionType.THE_END) {
							lightColor.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
						} else {
							Vector3f vector3f2 = vector3f.copy();
							vector3f2.mul(skyLight);
							lightColor.add(vector3f2);
							lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
							if (this.entityRenderer.getBossColorModifier(partialTicks) > 0.0F) {
								float bossMod = this.entityRenderer.getBossColorModifier(partialTicks);
								Vector3f vector3f3 = lightColor.copy();
								vector3f3.mul(0.7F, 0.6F, 0.6F);
								lightColor.lerp(vector3f3, bossMod);
							}
						}

						clientworld.getDimension().getLightmapColors(partialTicks, sun, skyLight, blockLight, lightColor);

						lightColor.clamp(0.0F, 1.0F);
						if (brightness > 0.0F) {
							float f10 = Math.max(lightColor.getX(), Math.max(lightColor.getY(), lightColor.getZ()));
							if (f10 < 1.0F) {
								float f12 = 1.0F / f10;
								Vector3f vector3f5 = lightColor.copy();
								vector3f5.mul(f12);
								lightColor.lerp(vector3f5, brightness);
							}
						}

						if (clientworld.getDimension().getType() != ETDims.DARK_TYPE) {
							float gamma = (float) this.client.gameSettings.gamma;
							Vector3f vector3f4 = lightColor.copy();
							vector3f4.apply(this::invGamma);
							lightColor.lerp(vector3f4, gamma);
							lightColor.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
						}

						lightColor.clamp(0.0F, 1.0F);
						lightColor.mul(255.0F);
						int r = (int)lightColor.getX();
						int g = (int)lightColor.getY();
						int b = (int)lightColor.getZ();
						this.nativeImage.setPixelRGBA(j, i, 0xFF000000 | b << 16 | g << 8 | r);
					}
				}

				this.dynamicTexture.updateDynamicTexture();
				this.client.getProfiler().endSection();
			}
		}
	}

	private float invGamma(float valueIn) {
		float f = 1.0F - valueIn;
		return 1.0F - f * f * f * f;
	}

	private float getLightBrightness(World worldIn, int lightLevelIn) {
		return worldIn.dimension.getLightBrightness(lightLevelIn);
	}

	public static int packLight(int blockLightIn, int skyLightIn) {
		return blockLightIn << 4 | skyLightIn << 20;
	}

	public static int getLightBlock(int packedLightIn) {
		return (packedLightIn & 0xFFFF) >> 4; // Forge: Fix fullbright quads showing dark artifacts. Reported as MC-169806
	}

	public static int getLightSky(int packedLightIn) {
		return packedLightIn >> 20 & '\uffff';
	}
}
