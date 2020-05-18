package white_blizz.ender_torment.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Ref.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShaderHandler implements AutoCloseable {
	private static ShaderHandler INSTANCE;


	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) throws IOException {
		Minecraft mc = Minecraft.getInstance();

		ClientPlayerEntity player = mc.player;
		if (player == null) return;
		if (player.isPotionActive(ETEffects.FLUX_VISION.get())) {
			if (INSTANCE == null) INSTANCE = new ShaderHandler(
					null,
					new ILayerInfo() {
						@Override public boolean testEntity(Entity entity) { return entity instanceof IMob; }
						//@Override public boolean testBlock(World world, BlockPos pos) { return false; }
						@Override public int getColor() { return 0xFF0000; }
					},
					new ILayerInfo() {
						@Override public boolean testEntity(Entity entity) { return entity instanceof AnimalEntity; }
						//@Override public boolean testBlock(World world, BlockPos pos) { return world.getBlockState(pos).getBlock() == Blocks.LEVER; }
						@Override public int getColor() { return 0x00FF00; }
					}
			);
			INSTANCE.render(event.getMatrixStack(), event.getPartialTicks());
		} else if (INSTANCE != null) {
			INSTANCE.close();
			INSTANCE = null;
		}
	}

	private static ResourceLocation getPost(String name) {
		return Ref.loc("shaders/post/", name, "json");
	}

	private static Framebuffer addAndGetFrameBuffer(ShaderGroup shaderGroup, String name, int width, int height) {
		shaderGroup.addFramebuffer(name, width, height);
		return shaderGroup.getFramebufferRaw(name);
	}

	private final ShaderGroup grayer, outlineLayer;

	public interface ILayerInfo {
		boolean testEntity(Entity entity);
		//boolean testBlock(World world, BlockPos pos);
		int getColor();
	}

	@SuppressWarnings("PointlessBitwiseExpression") //It looks good...
	private static class Layer implements AutoCloseable {
		private final ShaderGroup outlineLayer;
		private final Framebuffer buff, buffOut;
		private final Predicate<Entity> entityPred;
		//private final BiPredicate<World, BlockPos> blockPred;

		private Layer(Minecraft mc, Framebuffer buffIn, Predicate<Entity> entityPred, /*BiPredicate<World, BlockPos> blockPred,*/ int color) throws IOException {
			this.entityPred = entityPred;
			//this.blockPred = blockPred;
			int width = buffIn.framebufferWidth;
			int height = buffIn.framebufferHeight;
			//this.buffIn = buffIn;
			outlineLayer = new ShaderGroup(
					mc.textureManager,
					mc.getResourceManager(),
					buffIn,
					getPost("empty")
			);
			this.buffOut = addAndGetFrameBuffer(outlineLayer, "out", width, height);
			this.buff = addAndGetFrameBuffer(outlineLayer, "entity", width, height);
			Framebuffer frameB = addAndGetFrameBuffer(outlineLayer, "b", width, height);
			Shader outliner = outlineLayer.addShader(Ref.locStr("outline"), buff, frameB);
			ShaderUniform outlineColor = outliner.getShaderManager().func_216539_a("OutlineColor");
			if (outlineColor != null) {
				int r = (color >> 16) & 255;
				int g = (color >>  8) & 255;
				int b = (color >>  0) & 255;
				outlineColor.set(
						r / 255F,
						g / 255F,
						b / 255F
				);
			}
			Shader layerer = outlineLayer.addShader(Ref.locStr("layer"), buffIn, buffOut);
			layerer.addAuxFramebuffer("Layer2", frameB, width, height);
		}

		@Override public void close() { outlineLayer.close(); }
	}

	private final List<Layer> layers = new ArrayList<>();
	@Nullable private final Layer fallBack;

	private ShaderHandler(Integer fallBackColor, ILayerInfo... layerInfoList) throws IOException {
		Minecraft mc = Minecraft.getInstance();
		int w = mc.getMainWindow().getFramebufferWidth();
		int h = mc.getMainWindow().getFramebufferHeight();
		grayer = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				getPost("grey")
		);
		Shader combiner = grayer.addShader(Ref.locStr("combine"),
				grayer.getFramebufferRaw("grey"),
				mc.getFramebuffer());

		outlineLayer = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				getPost("empty")
		);

		final Framebuffer[] in = {new Framebuffer(w, h, true, Minecraft.IS_RUNNING_ON_MAC)};

		class C {
			Layer apply(Predicate<Entity> test1, /*BiPredicate<World, BlockPos> test2,*/ int color) throws IOException {
				Layer layer = new Layer(mc, in[0], test1, /*test2,*/ color);
				in[0] = layer.buffOut;

				return layer;
			}
		}
		C newLayer = new C();

		//for (ILayerInfo layerInfo : layerInfoList) layers.add(newLayer.apply(layerInfo::testEntity, /*layerInfo::testBlock,*/ layerInfo.getColor()));
		layers.add(newLayer.apply(ent -> ent instanceof IMob, 0xFF0000));
		layers.add(newLayer.apply(ent -> ent instanceof AnimalEntity, 0x00FF00));

		fallBack = fallBackColor != null ? newLayer.apply(ent -> true, /*(wd, p) -> true,*/ fallBackColor) : null;

		combiner.addAuxFramebuffer("Outlines", in[0], w, h);
		grayer.createBindFramebuffers(w, h);
		/*compile = new ShaderGroup(
				mc.textureManager,
				mc.getResourceManager(),
				mc.getFramebuffer(),
				getPost("compile")
		);
		compile.createBindFramebuffers(w, h);*/
	}

	private void render(MatrixStack matrixStack, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		ClientWorld world = mc.world;
		if (world != null && player != null) {
			int w = mc.getMainWindow().getFramebufferWidth();
			int h = mc.getMainWindow().getFramebufferHeight();
			Vec3d cam = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
			double cX = cam.getX();
			double cY = cam.getY();
			double cZ = cam.getZ();

			layers.forEach(layer -> layer.outlineLayer.createBindFramebuffers(w, h));
			if (fallBack != null) fallBack.outlineLayer.createBindFramebuffers(w, h);

			/*if (false) {
				int xzRange = 16;
				int yRange = 8;

				BlockPos playerPos = player.getPosition();

				for (int x = -xzRange; x <= xzRange; x++) {
					for (int z = -xzRange; z <= xzRange; z++) {
						for (int y = -yRange; y <= yRange; y++) {
							BlockPos pos = playerPos.add(x, y, z);
							Layer layer = layers.stream().filter(layer1 ->
									layer1.blockPred.test(world, pos)).findFirst().orElse(fallBack);
							Framebuffer buff;
							if (layer != null) buff = layer.buff;
							else return;
							buff.bindFramebuffer(true);

							mc.getBlockRendererDispatcher().renderModel(
									world.getBlockState(pos),
									pos,
									world,
									matrixStack,
									Tessellator.getInstance().getBuffer(),
									EmptyModelData.INSTANCE
							);

							buff.unbindFramebuffer();
						}
					}
				}
			}*/

			StreamSupport.stream(world.getAllEntities().spliterator(), false)
					.filter(ent -> !(ent instanceof PlayerEntity))
					.sorted((a, b) -> -Double.compare(a.getDistanceSq(cam), b.getDistanceSq(cam)))
					.forEach(entity -> {
						Layer layer = layers.stream().filter(layer1 ->
								layer1.entityPred.test(entity)).findFirst().orElse(fallBack);
						Framebuffer buff;
						if (layer != null) buff = layer.buff;
						else return;
						buff.bindFramebuffer(true);

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
						buff.unbindFramebuffer();

						/*ShaderUniform outlineColor = outliner.getShaderManager().func_216539_a("OutlineColor");
						if (outlineColor != null) {
							if (entity instanceof IMob) outlineColor.set(1F, 0F, 0F);
							else if (entity instanceof AnimalEntity) outlineColor.set(0F, 1F, 0F);
						}
						outliner.setProjectionMatrix(matrix4f);
						ShaderHandler.this.render(outliner, partialTicks);*/


					});
					/*.reduce(
							new Framebuffer(w, h, true, Minecraft.IS_RUNNING_ON_MAC),
							(a, b) -> {
								Framebuffer c = new Framebuffer(w, h, true, Minecraft.IS_RUNNING_ON_MAC);
								layerer.addAuxFramebuffer("Layer1", a, w, h);
								layerer.addAuxFramebuffer("Layer2", b, w, h);
								layerer.setProjectionMatrix(matrix4f);
								render(layerer, partialTicks);
								a.deleteFramebuffer();
								b.deleteFramebuffer();
								return c;
							}
					);*/

			layers.forEach(layer -> render(layer.outlineLayer, partialTicks));
			if (fallBack != null) render(fallBack.outlineLayer, partialTicks);

			//combiner.setProjectionMatrix(matrix4f);
			//render(combiner, partialTicks);
			grayer.createBindFramebuffers(w, h);
			render(grayer, partialTicks);


		}
	}

	private void render(Shader shader, float partialTicks) {
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		shader.render(partialTicks);
		RenderSystem.matrixMode(5889);
	}

	private void render(ShaderGroup group, float partialTicks) {
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.disableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.matrixMode(5890);
		RenderSystem.loadIdentity();
		group.render(partialTicks);
		RenderSystem.matrixMode(5889);
	}

	@Override
	public void close() {
		grayer.close();
		outlineLayer.close();
		layers.forEach(Layer::close);
		if (fallBack != null) fallBack.close();
		//compile.close();
	}
}
