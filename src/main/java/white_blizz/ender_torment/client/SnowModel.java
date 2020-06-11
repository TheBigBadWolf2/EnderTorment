package white_blizz.ender_torment.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.*;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.common.block.ISnowChain;
import white_blizz.ender_torment.common.block.StackingSnowBlock;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SnowModel implements IDynamicBakedModel {
	private static final int[] X = { 0, 0, 1, 1 };
	private static final int[] Z = { 0, 1, 1, 0 };
	private static final float eps = 1e-3f;
	static final ResourceLocation LOCATION_BLOCKS_TEXTURE = PlayerContainer.LOCATION_BLOCKS_TEXTURE;


	private static class UnoMap<K> extends HashMap<K, ImmutableMap<Direction, ImmutableList<BakedQuad>>> {
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> computeIfAbsent(
				K k1, UnoFunc<K> func
		) {
			return super.computeIfAbsent(k1, func::apply);
		}
	}
	private static class BiMap<K> extends HashMap<K, UnoMap<K>> {
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> computeIfAbsent(
				K k1, K k2, BiFunc<K> func
		) {
			return super.computeIfAbsent(k1, k -> new UnoMap<>()).computeIfAbsent(k2, func.down(k1));
		}
	}
	private static class TriMap<K> extends HashMap<K, BiMap<K>> {
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> computeIfAbsent(
				K k1, K k2, K k3, TriFunc<K> func
		) {
			return super.computeIfAbsent(k1, k -> new BiMap<>()).computeIfAbsent(k2, k3, func.down(k1));
		}
	}
	private static class QuadMap<K> extends HashMap<K, TriMap<K>> {
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> computeIfAbsent(
				K k1, K k2, K k3, K k4, QuadFunc<K> func
		) {
			return super.computeIfAbsent(k1, k -> new TriMap<>()).computeIfAbsent(k2, k3, k4, func.down(k1));
		}
	}

	private interface UnoFunc<K> {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(K k1);
	}
	private interface BiFunc<K> {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(K k1, K k2);
		default UnoFunc<K> down(K k1) { return (k2) -> apply(k1, k2); }
	}
	private interface TriFunc<K> {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(K k1, K k2, K k3);
		default BiFunc<K> down(K k1) { return (k2, k3) -> apply(k1, k2, k3); }
	}
	private interface QuadFunc<K> {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(K k1, K k2, K k3, K k4);
		default TriFunc<K> down(K k1) { return (k2, k3, k4) -> apply(k1, k2, k3, k4); }
	}

	private static abstract class SuperMap<K> {
		protected final QuadMap<K> map = new QuadMap<>();

		protected SuperMap() {}

		public abstract ModelProperty<K> getCorner(int x, int z);
		public ModelProperty<K> getCorner(int i) { return getCorner(X[i], Z[i]); }
		public abstract ImmutableMap<Direction, ImmutableList<BakedQuad>> get(IModelData data, SuperFunc<K> func);
	}

	private static class SuperIntMap extends SuperMap<Integer> {
		private static final int MAX = StackingSnowBlock.MAX;
		private static class IntProp extends ModelProperty<Integer> {
			private IntProp() { super(i -> 1 <= i && i <= MAX); }
		}
		private final IntProp[][] corners;
		public SuperIntMap() {
			corners = new IntProp[2][2];
			for (int x = 0; x < 2; x++) {
				for (int z = 0; z < 2; z++) {
					corners[x][z] = new IntProp();
				}
			}
		}

		@Override
		public IntProp getCorner(int x, int z) {
			return corners[x][z];
		}

		private int get(IModelData data, int i) {
			IntProp prop = getCorner(X[i], Z[i]);
			if (data.hasProperty(prop)) {
				Integer v = data.getData(prop);
				if (v != null) return v;
			}
			return MAX;
		}

		@Override
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> get(IModelData data, SuperFunc<Integer> func) {
			Integer[] corners = {
					get(data, 0),
					get(data, 1),
					get(data, 2),
					get(data, 3),
			};
			return map.computeIfAbsent(
					corners[0],
					corners[0],
					corners[0],
					corners[0],
					((k1, k2, k3, k4) -> func.apply(true, corners, -1001))
			);
		}
	}

	private static class SuperFloatMap extends SuperMap<Float> {
		private static final float MAX = 1F;
		private static class FloatProp extends ModelProperty<Float> {
			private FloatProp() { super(i -> 0 <= i && i <= MAX); }
		}
		private final FloatProp[][] corners;
		public SuperFloatMap() {
			corners = new FloatProp[2][2];
			for (int x = 0; x < 2; x++) {
				for (int z = 0; z < 2; z++) {
					corners[x][z] = new FloatProp();
				}
			}
		}

		@Override
		public FloatProp getCorner(int x, int z) {
			return corners[x][z];
		}

		private float get(IModelData data, int i) {
			FloatProp prop = getCorner(X[i], Z[i]);
			if (data.hasProperty(prop)) {
				Float v = data.getData(prop);
				if (v != null) return v;
			}
			return MAX;
		}

		@Override
		public ImmutableMap<Direction, ImmutableList<BakedQuad>> get(IModelData data, SuperFunc<Float> func) {
			Float[] corners = {
					get(data, 0),
					get(data, 1),
					get(data, 2),
					get(data, 3),
			};
			//return func.apply(true, corners, -1001);
			return map.computeIfAbsent(
					corners[0],
					corners[1],
					corners[2],
					corners[3],
					((k1, k2, k3, k4) -> func.apply(true, corners, -1001))
			);
		}
	}

	private interface SuperFunc<K> {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(boolean b, K[] corners, int i);
	}
	private interface SuperIntFunc extends SuperFunc<Integer> {
		@Override
		default ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(boolean b, Integer[] corners, int i) {
			int l = corners.length;
			int[] _corners= new int[l];
			for (int j = 0; j < l; j++) _corners[j] = corners[j];
			return apply(b, _corners, i);
		}

		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(boolean b, int[] corners, int i);
	}
	private interface SuperFloatFunc extends SuperFunc<Float> {
		@Override
		default ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(boolean b, Float[] corners, int i) {
			int l = corners.length;
			float[] _corners= new float[l];
			for (int j = 0; j < l; j++) _corners[j] = corners[j];
			return apply(b, _corners, i);
		}

		ImmutableMap<Direction, ImmutableList<BakedQuad>> apply(boolean b, float[] corners, int i);
	}

	private final TransformationMatrix transformation;
	private final TextureAtlasSprite topTexture;
	private final TextureAtlasSprite sideTexture;
	private final TextureAtlasSprite bottomTexture;
	private final SuperMap<Float> map = new SuperFloatMap();//new SuperMap<>(i -> 1 <= i && i <= StackingSnowBlock.MAX, Float[]::new, 1F);
	private final ImmutableMap<Direction, ImmutableList<BakedQuad>> defaultMap;

	public SnowModel(TransformationMatrix transformation, TextureAtlasSprite texture) {
		this.transformation = transformation;
		topTexture = sideTexture = bottomTexture = texture;
		defaultMap = buildQuads(false, new float[4], -1000);
	}

	public SnowModel(TransformationMatrix transformation, TextureAtlasSprite topTexture, TextureAtlasSprite sideTexture, TextureAtlasSprite bottomTexture) {
		this.transformation = transformation;
		this.topTexture = topTexture;
		this.sideTexture = sideTexture;
		this.bottomTexture = bottomTexture;
		defaultMap = buildQuads(false, new float[4], -1000);
	}


	@Nonnull
	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState state, @Nullable Direction side,
			@Nonnull Random rand, @Nonnull IModelData extraData) {
		ImmutableMap<Direction, ImmutableList<BakedQuad>> map;
		if (state == null) map = defaultMap;
		else map = this.map.get(extraData, (SuperFloatFunc) this::buildQuads);

		if (side != null) return map.get(side);
		//return map.values().stream().flatMap(Collection::stream).collect(ImmutableList.toImmutableList());
		return ImmutableList.of();
	}

	boolean ceiling;

	private ImmutableMap<Direction, ImmutableList<BakedQuad>> buildQuads(boolean statePresent, float[] cornerRound, int flowRound) {
		EnumMap<Direction, ImmutableList<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
		for (Direction side : Direction.values()) faceQuads.put(side, ImmutableList.of());

		if (statePresent) {
			// y levels
			float[] y = new float[4];
			boolean fullVolume = true;
			for (int i = 0; i < 4; i++) {
				float value = cornerRound[i];
				if (value < 1f) fullVolume = false;
				y[i] = ceiling ? 1f - value : value;
			}

			// flow
			boolean isFlowing = flowRound > -1000;

			float flow = isFlowing ? (float) Math.toRadians(flowRound) : 0f;
			TextureAtlasSprite topSprite = ceiling ? bottomTexture : topTexture;
			float scale = isFlowing ? 4f : 8f;

			float c = MathHelper.cos(flow) * scale;
			float s = MathHelper.sin(flow) * scale;

			// top
			Direction top = ceiling ? Direction.DOWN : Direction.UP;

			// base uv offset for flow direction
			VertexParameter uv = i -> c * (X[i] * 2 - 1) + s * (Z[i] * 2 - 1);

			VertexParameter topX = i -> X[i];
			VertexParameter topY = i -> y[i];
			VertexParameter topZ = i -> Z[i];
			VertexParameter topU = i -> 8 + uv.get(i);
			VertexParameter topV = i -> 8 + uv.get((i + 1) % 4);

			{
				ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

				builder.add(buildQuad(top, topSprite, ceiling, false, topX, topY, topZ, topU, topV));
				if (!fullVolume) builder.add(buildQuad(top, topSprite, !ceiling, true, topX, topY, topZ, topU, topV));

				faceQuads.put(top, builder.build());
			}

			// bottom
			Direction bottom = top.getOpposite();
			faceQuads.put(bottom, ImmutableList.of(
					buildQuad(bottom, ceiling ? topTexture : bottomTexture, ceiling, false,
							i -> Z[i],
							i -> ceiling ? 1 : 0,
							i -> X[i],
							i -> Z[i] * 16,
							i -> X[i] * 16
					)
			));

			// sides
			for (int i = 0; i < 4; i++) {
				Direction side = Direction.byHorizontalIndex((5 - i) % 4); // [W, S, E, N]
				int si = i; // local var for lambda capture

				VertexParameter sideX = j -> X[(si + X[j]) % 4];
				VertexParameter sideY = j -> Z[j] == 0 ? (ceiling ? 1 : 0) : y[(si + X[j]) % 4];
				VertexParameter sideZ = j -> Z[(si + X[j]) % 4];
				VertexParameter sideU = j -> X[j] * 8;
				VertexParameter sideV = j -> (ceiling ? sideY.get(j) : 1 - sideY.get(j)) * 8;

				ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

				if (sideTexture != null) {
					builder.add(buildQuad(side, sideTexture, ceiling, true, sideX, sideY, sideZ, sideU, sideV));
					builder.add(buildQuad(side, sideTexture, !ceiling, false, sideX, sideY, sideZ, sideU, sideV));
				}/* else {
					VertexParameter a = j -> 1 - sideY.get(j);
					builder.add(buildQuad(side, topTexture, ceiling, true, sideX, sideY, sideZ, sideU, sideV, a));
					builder.add(buildQuad(side, topTexture, !ceiling, false, sideX, sideY, sideZ, sideU, sideV, a));

					a = j -> sideY.get(j);
					a = j -> 0.5F;
					builder.add(buildQuad(side, bottomTexture, ceiling, true, sideX, sideY, sideZ, sideU, sideV));
					builder.add(buildQuad(side, bottomTexture, !ceiling, false, sideX, sideY, sideZ, sideU, sideV, a));
				}*/
				faceQuads.put(side, builder.build());
			}
		} else {
			// inventory
			faceQuads.put(Direction.SOUTH, ImmutableList.of(
					buildQuad(Direction.UP, getParticleTexture(), false, false,
							i -> Z[i],
							i -> X[i],
							i -> 0,
							i -> Z[i] * 16,
							i -> X[i] * 16
					)
			));
		}

		return ImmutableMap.copyOf(faceQuads);
	}

	// maps vertex index to parameter value
	private interface VertexParameter { float get(int index);}

	private BakedQuad buildQuad(Direction side, TextureAtlasSprite texture, boolean flip, boolean offset, VertexParameter x, VertexParameter y, VertexParameter z, VertexParameter u, VertexParameter v) {
		return buildQuad(side, texture, flip, offset, x, y, z, u, v, i -> 1F);
	}
	private BakedQuad buildQuad(Direction side, TextureAtlasSprite texture, boolean flip, boolean offset, VertexParameter x, VertexParameter y, VertexParameter z, VertexParameter u, VertexParameter v, VertexParameter a) {
		BakedQuadBuilder builder = new BakedQuadBuilder(texture);

		builder.setQuadOrientation(side);
		builder.setQuadTint(0);

		boolean hasTransform = !transformation.isIdentity();
		IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transformation) : builder;

		for (int i = 0; i < 4; i++) {
			int vertex = flip ? 3 - i : i;
			putVertex(
					consumer, side, offset,
					x.get(vertex), y.get(vertex), z.get(vertex),
					texture.getInterpolatedU(u.get(vertex)),
					texture.getInterpolatedV(v.get(vertex)),
					a.get(i)
			);
		}

		return builder.build();
	}

	private void putVertex(IVertexConsumer consumer, Direction side, boolean offset, float x, float y, float z, float u, float v, float alpha) {
		VertexFormat format = DefaultVertexFormats.BLOCK;
		ImmutableList<VertexFormatElement> elements = format.getElements();
		for(int e = 0; e < elements.size(); e++) {
			switch(elements.get(e).getUsage()) {
				case POSITION:
					float dx = offset ? side.getDirectionVec().getX() * eps : 0f;
					float dy = offset ? side.getDirectionVec().getY() * eps : 0f;
					float dz = offset ? side.getDirectionVec().getZ() * eps : 0f;
					consumer.put(e, x - dx, y - dy, z - dz, 1f);
					break;
				case COLOR:
					consumer.put(e, 1F, 1F, 1F, alpha);
					break;
				case NORMAL:
					float offX = (float) side.getXOffset();
					float offY = (float) side.getYOffset();
					float offZ = (float) side.getZOffset();
					consumer.put(e, offX, offY, offZ, 0f);
					break;
				case UV:
					if(elements.get(e).getIndex() == 0)
					{
						consumer.put(e, u, v, 0f, 1f);
						break;
					}
					// else fallthrough to default
				default:
					consumer.put(e);
					break;
			}
		}
	}


	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
		ModelDataMap.Builder builder = new ModelDataMap.Builder();

		for (int i = 0; i < 4; i++) {
			int x = X[i] * 2 - 1;
			int z = Z[i] * 2 - 1;

			class C {
				boolean maxed = false;
				int corner = 0;
				int count = 0;

				C() {
					if (state.getBlock() instanceof ISnowChain) {
						maxed |= ((ISnowChain) state.getBlock()).isMax(world, pos, state);
						corner += ((ISnowChain) state.getBlock()).getLevel(world, pos, state);
						count++;
					}
				}

				void test(boolean doX, boolean doZ) {
					BlockPos pos1 = pos.add(doX ? x : 0, 0, doZ ? z : 0);
					BlockState state1 = world.getBlockState(pos1);
					if (state1.getBlock() instanceof ISnowChain) {
						maxed = maxed || ((ISnowChain) state1.getBlock()).isMax(world, pos1, state1);
						corner += ((ISnowChain) state1.getBlock()).getLevel(world, pos1, state1);
						count++;
					}
				}
			}
			C tester = new C();

			tester.test(true, false);
			tester.test(false, true);
			if (tester.count > 1) tester.test(true, true);

			if (tester.maxed) builder.withInitial(map.getCorner(i), 1F);
			else builder.withInitial(map.getCorner(i), (float)tester.corner / (float)tester.count / (float) ISnowChain.MAX);
		}
		return builder.build();
	}

	@Override public boolean isAmbientOcclusion() { return false; }

	@Override public boolean isGui3d() { return false; }
	@Override public boolean func_230044_c_() { return false; } //Gui3DDiffuseLighting?
	@Override public boolean isBuiltInRenderer() { return true; }

	@Override public TextureAtlasSprite getParticleTexture() { return sideTexture != null ? sideTexture : topTexture; }
	@Override public ItemOverrideList getOverrides() { return ItemOverrideList.EMPTY; }

	interface TextureConfig {
		Material getTop();
		Material getSide();
		Material getBottom();

		default Collection<Material> getTextures() {
			return Stream.of(getTop(), getSide(), getBottom())
					.distinct()
					.collect(ImmutableList.toImmutableList());
		}

		SnowModel make(TransformationMatrix transformation, Function<Material, TextureAtlasSprite> spriteGetter);
	}

	private static class SingleTextureConfig implements TextureConfig {
		private final Material mat;

		private SingleTextureConfig(Material mat) { this.mat = mat; }

		@Override public Material getTop() { return mat; }
		@Override public Material getSide() { return mat; }
		@Override public Material getBottom() { return mat; }

		@Override
		public SnowModel make(TransformationMatrix transformation, Function<Material, TextureAtlasSprite> spriteGetter) {
			return new SnowModel(transformation, spriteGetter.apply(mat));
		}
	}

	static class Geometry implements IModelGeometry<Geometry> {
		//private final Material mat = new Material(LOCATION_BLOCKS_TEXTURE, Ref.MC.rl.loc("block","snow"));
		//private final Material top, side, bottom;
		private final TextureConfig config;

		Geometry(TextureConfig config) {
			this.config = config;
		}


		@Override
		public IBakedModel bake(
				IModelConfiguration owner,
				ModelBakery bakery,
				Function<Material, TextureAtlasSprite> spriteGetter,
				IModelTransform modelTransform,
				ItemOverrideList overrides,
				ResourceLocation modelLocation) {
			return config.make(modelTransform.getRotation(), spriteGetter);
		}

		@Override
		public Collection<Material> getTextures(
				IModelConfiguration owner,
				Function<ResourceLocation, IUnbakedModel> modelGetter,
				Set<Pair<String, String>> missingTextureErrors) {
			return config.getTextures();
		}
	}

	@SuppressWarnings("PointlessBitwiseExpression")
	static class Loader implements IModelLoader<Geometry> {

		private IResourceManager manager;
		private final Pack pack = Pack.INSTANCE;

		private static int[] split(int i) {
			return new int[]{
					(i >>  0) & 255,
					(i >>  8) & 255,
					(i >> 16) & 255,
					(i >> 24) & 255,
			};
		}

		private static int[] trans(int[] array, IntUnaryOperator op) {
			int[] next = new int[4];
			for (int i = 0; i < 4; i++) next[i] = op.applyAsInt(array[i]);
			return next;
		}

		private static int[] combine(int[] a, int[] b) {
			int[] c = new int[4];
			for (int i = 0; i < 4; i++) c[i] = a[i] + b[i];
			return c;
		}

		private static int clamp(double d) {
			return MathHelper.clamp(MathHelper.floor(d), 0, 255);
		}

		private static int recombine(int[] array) {
			return  (array[0] <<  0) |
					(array[1] <<  8) |
					(array[2] << 16) |
					(array[3] << 24) ;
		}

		private void setManager(IResourceManager resourceManager) {
			manager = resourceManager;
			if (manager instanceof SimpleReloadableResourceManager)
				((SimpleReloadableResourceManager) manager).addResourcePack(pack);
			if (manager instanceof FallbackResourceManager)
				((FallbackResourceManager) manager).addResourcePack(pack);
		}

		Loader() { setManager(Minecraft.getInstance().getResourceManager()); }

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			setManager(resourceManager);
		}

		private static ResourceLocation rl(String name) {
			return new ResourceLocation(name);
		}

		private static Material newMaterial(String texture) {
			return newMaterial(rl(texture));
		}

		private static Material newMaterial(ResourceLocation texture) {
			return new Material(LOCATION_BLOCKS_TEXTURE, texture);
		}

		private NativeImage get(String name) throws IOException {
			ResourceLocation rl = rl(name);
			rl = new ResourceLocation(rl.getNamespace(), "textures/"+rl.getPath()+".png");
			IResource resource = manager.getResource(rl);
			return NativeImage.read(resource.getInputStream());
		}

		@Override
		public Geometry read(JsonDeserializationContext deserializationContext,
							 JsonObject modelContents) {
			TextureConfig config = null;
			if (modelContents.has("texture")) {
				Material mat = newMaterial(modelContents.getAsJsonPrimitive("texture").getAsString());
				config = new SingleTextureConfig(mat);
			} else {
				String topStr, sideStr = null, bottomStr;
				Material top, side = null, bottom;
				topStr = modelContents.getAsJsonPrimitive("top").getAsString();
				bottomStr = modelContents.getAsJsonPrimitive("bottom").getAsString();
				if (modelContents.has("side"))
					sideStr = modelContents.getAsJsonPrimitive("side").getAsString();

				top = newMaterial(topStr);
				bottom = newMaterial(bottomStr);

				if (sideStr != null) side = newMaterial(sideStr);
				else if (topStr.equals(bottomStr)) config = new SingleTextureConfig(top);
				else {
					try (
							NativeImage topImage = get(topStr);
							NativeImage bottomImage = get(bottomStr)
					) {
						if (topImage.getHeight() == bottomImage.getHeight()
								&& topImage.getWidth() == bottomImage.getWidth()) {
							NativeImage sideImage = new NativeImage(topImage.getWidth(),
									topImage.getHeight(), true);

							int xMax = topImage.getWidth() - 1;
							int yMax = topImage.getHeight() - 1;

							for (int y = 0; y <= yMax; y++) {
								double b = (double)y / (double)yMax;
								double a = 1D - b;
								IntUnaryOperator aOps[] = {
										c -> 0,
										c -> clamp(c * a),
										c -> clamp(c * a),
								}, bOps[] = {
										c -> clamp(c * b),
										c -> 0,
										c -> clamp(c * b),
								}, index = i -> Math.min(i/3, 2);
								for (int x = 0; x <= xMax; x++) {
									int i = index.applyAsInt(x);
									IntUnaryOperator aOp = aOps[i], bOp = bOps[i];
									sideImage.setPixelRGBA(x, y, recombine(combine(
										trans(split(topImage.getPixelRGBA(x, y)), aOp),
										trans(split(bottomImage.getPixelRGBA(x, y)), bOp)
									)));
								}
							}
							UnaryOperator<String> cleanup = str -> str
									.replace('/', '-')
									.replace(':', '-');
							ResourceLocation sideRL = new ResourceLocation(Ref.MOD_ID, String.format(
									"%s_2_%s",
									cleanup.apply(topStr),
									cleanup.apply(bottomStr)
							));
							pack.add(sideRL, sideImage);
							side = newMaterial(sideRL);
						}
					} catch (IOException e) {
						EnderTorment.LOGGER.error("Could not load textures", e);
						//throw new RuntimeException("Could not load textures", e);
					} finally { if (side == null) side = newMaterial("missingno"); }
				}

				if (config == null) {
					Material finalSide = side;
					config = (new TextureConfig() {
						@Override
						public Material getTop() {
							return top;
						}

						@Override
						public Material getSide() {
							return finalSide;
						}

						@Override
						public Material getBottom() {
							return bottom;
						}

						@Override
						public SnowModel make(TransformationMatrix transformation,
											  Function<Material, TextureAtlasSprite> spriteGetter) {
							return new SnowModel(transformation,
									spriteGetter.apply(top),
									spriteGetter.apply(finalSide),
									spriteGetter.apply(bottom)
							);
						}
					});
				}
			}
			return new Geometry(config);
		}
	}
}
