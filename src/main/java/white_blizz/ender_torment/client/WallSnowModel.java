package white_blizz.ender_torment.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import white_blizz.ender_torment.common.math.*;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WallSnowModel implements IDynamicBakedModel {
	private static final int[] X = { 0, 0, 1, 1 };
	private static final int[] Z = { 0, 1, 1, 0 };
	private static final float eps = 1e-3f;
	static final ResourceLocation LOCATION_BLOCKS_TEXTURE = PlayerContainer.LOCATION_BLOCKS_TEXTURE;

	private final TransformationMatrix transformation;
	private final TextureAtlasSprite texture;
	private final ItemOverrideList overrideList;

	private interface Vec2Float {
		float get(IVec3f vec);
	}

	private interface VectorParameter {
		IVec3f get(int index);

		default VertexParameter andThen(Vec2Float func) {
			return i -> func.get(get(i));
		}
	}

	private static void flatten() {

	}

	public class ModelBox {
		private final TexturedQuad[] quads;

		public ModelBox(IVec3f[][][] points) {
			this.quads = new TexturedQuad[6];

			PositionTextureVertex vertex7 = new PositionTextureVertex(points[0][0][0]);
			PositionTextureVertex vertex3 = new PositionTextureVertex(points[0][0][1]);
			PositionTextureVertex vertex2 = new PositionTextureVertex(points[0][1][0]);
			PositionTextureVertex vertex6 = new PositionTextureVertex(points[0][1][1]);
			PositionTextureVertex vertex0 = new PositionTextureVertex(points[1][0][0]);
			PositionTextureVertex vertex4 = new PositionTextureVertex(points[1][0][1]);
			PositionTextureVertex vertex1 = new PositionTextureVertex(points[1][1][0]);
			PositionTextureVertex vertex5 = new PositionTextureVertex(points[1][1][1]);

			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex3, vertex7, vertex0}, IVec3f::getX, IVec3f::getZ, Direction.DOWN);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{vertex1, vertex2, vertex6, vertex5}, IVec3f::getX, IVec3f::getZ, Direction.UP);
			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{vertex0, vertex7, vertex2, vertex1}, IVec3f::getX, IVec3f::getY, Direction.NORTH);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{vertex3, vertex4, vertex5, vertex6}, IVec3f::getX, IVec3f::getY, Direction.SOUTH);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{vertex7, vertex3, vertex6, vertex2}, IVec3f::getY, IVec3f::getZ, Direction.WEST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex0, vertex1, vertex5}, IVec3f::getY, IVec3f::getZ, Direction.EAST);
		}
		public ModelBox(float x, float y, float z, float width, float height, float depth) {
			this.quads = new TexturedQuad[6];
			float x1 = x + width;
			float y1 = y + height;
			float z1 = z + depth;

			PositionTextureVertex vertex7 = new PositionTextureVertex( x,  y,  z);
			PositionTextureVertex vertex3 = new PositionTextureVertex( x,  y, z1);
			PositionTextureVertex vertex2 = new PositionTextureVertex( x, y1,  z);
			PositionTextureVertex vertex6 = new PositionTextureVertex( x, y1, z1);
			PositionTextureVertex vertex0 = new PositionTextureVertex(x1,  y,  z);
			PositionTextureVertex vertex4 = new PositionTextureVertex(x1,  y, z1);
			PositionTextureVertex vertex1 = new PositionTextureVertex(x1, y1,  z);
			PositionTextureVertex vertex5 = new PositionTextureVertex(x1, y1, z1);

			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex3, vertex7, vertex0}, Direction.DOWN);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{vertex1, vertex2, vertex6, vertex5}, Direction.UP);
			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{vertex0, vertex7, vertex2, vertex1}, Direction.NORTH);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{vertex3, vertex4, vertex5, vertex6}, Direction.SOUTH);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{vertex7, vertex3, vertex6, vertex2}, Direction.WEST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex0, vertex1, vertex5}, Direction.EAST);
		}

		public BakedQuad baked(Direction side) {
			return quads[side.getIndex()].bake();
		}
	}

	private static class PositionTextureVertex {
		private static final float SCALE = 16;

		public final IVec3f position;
		public final float textureU;
		public final float textureV;
		public final Vec2Float textureUGetter, textureVGetter;

		public PositionTextureVertex(float x, float y, float z) { this(x, y, z, 0, 0); }
		public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
			this(new MutVec3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex(IVec3f posIn) { this(posIn, 0, 0); }
		public PositionTextureVertex(IVec3f posIn, float texU, float texV) {
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
			this.textureUGetter = null;
			this.textureVGetter = null;
		}

		public PositionTextureVertex(IVec3f posIn, Vec2Float texU, Vec2Float texV) {
			position = posIn;
			textureUGetter = texU;
			textureVGetter = texV;
			textureU = textureV = 0;
		}

		public PositionTextureVertex setTextureUV(float texU, float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}
		public PositionTextureVertex setTextureUV(Vec2Float texU, Vec2Float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public float getTextureU() {
			if (textureUGetter != null) return textureUGetter.get(position) * SCALE;
			return textureU;
		}

		public float getTextureV() {
			if (textureVGetter != null) return textureVGetter.get(position) * SCALE;
			return textureV;
		}

	}

	private class TexturedQuad {
		public final PositionTextureVertex[] vertexPositions;
		private final Direction side;
		public final net.minecraft.client.renderer.Vector3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, Direction directionIn) {
			this(positionsIn, 0, 0, 16, 16, directionIn);
		}
		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, Direction directionIn) {
			this.vertexPositions = positionsIn;
			side = directionIn;
			positionsIn[0] = positionsIn[0].setTextureUV(u2, v1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1, v1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1, v2);
			positionsIn[3] = positionsIn[3].setTextureUV(u2, v2);

			this.normal = directionIn.toVector3f();
		}
		public TexturedQuad(PositionTextureVertex[] positionsIn, Vec2Float u, Vec2Float v, Direction directionIn) {
			this.vertexPositions = positionsIn;
			side = directionIn;
			for (int i = 0; i < 4; i++) {
				positionsIn[i] = positionsIn[i].setTextureUV(u, v);
			}

			this.normal = directionIn.toVector3f();
		}

		public BakedQuad bake() {
			return buildQuad(side, texture,
					false, false,
					i -> vertexPositions[i].position.getX(),
					i -> vertexPositions[i].position.getY(),
					i -> vertexPositions[i].position.getZ(),
					i -> vertexPositions[i].getTextureU(),
					i -> vertexPositions[i].getTextureV()
			);
		}

	}

	private final ModelBox[][][] boxes = new ModelBox[3][3][3];
	private final IVec3f[][][] points = new IVec3f[4][4][4];

	public WallSnowModel(TransformationMatrix transformation, TextureAtlasSprite texture, ItemOverrideList overrideList) {
		this.transformation = transformation;
		this.texture = texture;
		this.overrideList = overrideList;

		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 4; z++) {
					IMutVec3f vec = new MutVec3f(x / 3F, y / 3F, z / 3F);
					if (x == 0 || x == 3) vec = vec.withLock(Lock.Type.STATIC, Lock.Axis.X);
					else vec = vec.withLock(Lock.Type.SLIDING, Lock.Axis.X);
					if (y == 0 || y == 3) vec = vec.withLock(Lock.Type.STATIC, Lock.Axis.Y);
					else vec = vec.withLock(Lock.Type.SLIDING, Lock.Axis.Y);
					if (z == 0 || z == 3) vec = vec.withLock(Lock.Type.STATIC, Lock.Axis.Z);
					else vec = vec.withLock(Lock.Type.SLIDING, Lock.Axis.Z);
					points[x][y][z] = vec;
				}
			}
		}

		for (int x = 1; x < 3; x++) {
			for (int y = 1; y < 3; y++) {
				for (int z = 1; z < 3; z++) {
					points[x][y][z] = new CalcVec3f(
							points[0][y][z],
							points[3][y][z],
							points[x][0][z],
							points[x][3][z],
							points[x][y][0],
							points[x][y][3]
					);
				}
			}
		}

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {
					IVec3f[][][] boxPoints = new IVec3f[2][2][2];

					for (int x1 = 0; x1 < 2; x1++) {
						for (int y1 = 0; y1 < 2; y1++) {
							System.arraycopy(points[x + x1][y + y1], z, boxPoints[x1][y1], 0, 2);
						}
					}
					boxes[x][y][z] = new ModelBox(boxPoints);
				}
			}
		}
	}

	private ModelBox getBox(Vec3i vec) { return getBox(vec.getX(), vec.getY(), vec.getZ()); }

	private ModelBox getBox(int x, int y, int z) {
		return boxes[x+1][y+1][z+1];
	}

	private List<BakedQuad> get(@Nullable Direction side) {
		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

		if (side == null) {
			for (Direction dir : Direction.values()) {
				builder.add(getBox(dir.getDirectionVec()).baked(dir.getOpposite()));
			}
		} else {
			Supplier<Stream<Integer>> ints = () -> Stream.iterate(-1, i -> i + 1);
			Vec3i vec = side.getDirectionVec();
			Streams.zip(ints.get(),
					Arrays.stream(boxes),
					(i, bs) -> Pair.of(new BlockPos(i, 0, 0), bs)
			)
					.flatMap(pair -> Streams.zip(ints.get(),
					Arrays.stream(pair.getSecond()),
							(i, bs) -> pair.mapFirst(v -> v.add(0, i, 0)).mapSecond(o -> bs)
			))
					.flatMap(pair -> Streams.zip(ints.get(),
					Arrays.stream(pair.getSecond()),
							(i, bs) -> pair.mapFirst(v -> v.add(0, 0, i)).mapSecond(o -> bs)
			))
			.filter(pair -> {
				BlockPos pos = pair.getFirst();
				return (pos.getX() == vec.getX() && vec.getX() != 0)
						|| (pos.getY() == vec.getY() && vec.getY() != 0)
						|| (pos.getZ() == vec.getZ() && vec.getZ() != 0);
			}).map(Pair::getSecond)
			.map(box -> box.baked(side))
			.forEach(builder::add);
		}
		return builder.build();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state,
									@Nullable Direction side,
									@Nonnull Random rand,
									@Nonnull IModelData extraData) {
		/*Arrays.stream(boxes)
				.flatMap(Arrays::stream)
				.flatMap(Arrays::stream)*/
		/*try { if (side != null) return ImmutableList.of(
				boxes[1][1][1].baked(side)
		); }
		catch (Throwable ignored) {}


		return ImmutableList.of();*/
		return get(side);
		//return ImmutableList.of();
	}

	private interface VertexParameter { float get(int index);}



	/*private Box buildQuads(
			boolean statePresent, float[][] corners) {

	}*/

	private BakedQuad buildQuad(Direction side, TextureAtlasSprite texture, boolean flip, boolean offset, VertexParameter x, VertexParameter y, VertexParameter z, VertexParameter u, VertexParameter v) {
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
					texture.getInterpolatedV(v.get(vertex))
			);
		}

		return builder.build();
	}

	private void putVertex(IVertexConsumer consumer, Direction side, boolean offset, float x, float y, float z, float u, float v) {
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
					consumer.put(e, 1F, 1F, 1F, 1F);
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
		return tileData;
	}

	@Override public boolean isAmbientOcclusion() { return false; }
	@Override public boolean isGui3d() { return false; }
	@Override public boolean func_230044_c_() { return false; }
	@Override public boolean isBuiltInRenderer() { return true; }
	@Override public TextureAtlasSprite getParticleTexture() { return texture; }
	@Override public ItemOverrideList getOverrides() { return overrideList; }

	static class Geometry implements IModelGeometry<Geometry> {
		private final Material mat = new Material(LOCATION_BLOCKS_TEXTURE, Ref.MC.rl.loc("block", "snow"));

		@Override
		public IBakedModel bake(IModelConfiguration owner,
								ModelBakery bakery,
								Function<Material, TextureAtlasSprite> spriteGetter,
								IModelTransform modelTransform,
								ItemOverrideList overrides,
								ResourceLocation modelLocation) {
			return new WallSnowModel(modelTransform.getRotation(), spriteGetter.apply(mat), overrides);
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner,
												Function<ResourceLocation, IUnbakedModel> modelGetter,
												Set<Pair<String, String>> missingTextureErrors) {
			return ImmutableList.of(mat);
		}
	}

	static class Loader implements IModelLoader<Geometry> {

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {

		}

		@Override
		public Geometry read(JsonDeserializationContext deserializationContext,
							 JsonObject modelContents) {
			return new Geometry();
		}
	}
}
