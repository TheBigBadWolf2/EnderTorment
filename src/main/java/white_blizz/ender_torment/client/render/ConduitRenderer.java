package white_blizz.ender_torment.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import white_blizz.ender_torment.client.ClientConfig;
import white_blizz.ender_torment.client.render.shader.ETShaderInstance;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.conduit.Link;
import white_blizz.ender_torment.common.conduit.Node;
import white_blizz.ender_torment.common.tile_entity.ConduitTE;

import javax.annotation.ParametersAreNonnullByDefault;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static white_blizz.ender_torment.client.render.ETRenderType.CONDUIT;
import static white_blizz.ender_torment.client.render.ETRenderType.OVERLAY_LINES;
import static white_blizz.ender_torment.client.render.ModelConduit.ConnectionType;

@SuppressWarnings("PointlessBitwiseExpression")
@ParametersAreNonnullByDefault
public class ConduitRenderer extends TileEntityRenderer<ConduitTE> {
	private static final boolean CASHE = false; //set to false for debugging.

	static final Logger LOGGER = LogManager.getLogger();

	static final Map<ConduitType<?>, ModelConduit> MAP = new HashMap<>();
	static float TIME = 0;

	private static ModelBox newBox(Vector3f pos, Vector3f size) {
		float x = pos.getX(), y = pos.getY(), z = pos.getZ();
		float w = size.getX(), h = size.getY(), d = size.getZ();

		if (w < 0) {
			x += w;
			w = -w;
		}
		if (h < 0) {
			y += h;
			h = -h;
		}
		if (d < 0) {
			z += d;
			d = -d;
		}
		return new ModelBox(x, y, z, w, h, d);
	}

	public static class ModelBox {
		private static final int[] map = {
				2, 3, 4, 5, 1, 0
		};



		private final TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		public TexturedQuad getSide(Direction side) {
			return quads[map[side.getIndex()]];
		}

		public ModelBox(float x, float y, float z, float width, float height, float depth) {
			this(x, y, z, width, height, depth, 0, 0, 0);
		}
		public ModelBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ) {
			this(0, 0, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, false, 0, 0);
		}
		public ModelBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, float texWidth, float texHeight) {
			this.posX1 = x;
			this.posY1 = y;
			this.posZ1 = z;
			this.posX2 = x + width;
			this.posY2 = y + height;
			this.posZ2 = z + depth;
			this.quads = new TexturedQuad[6];
			float x2 = x + width;
			float y2 = y + height;
			float z2 = z + depth;
			x = x - deltaX;
			y = y - deltaY;
			z = z - deltaZ;
			x2 = x2 + deltaX;
			y2 = y2 + deltaY;
			z2 = z2 + deltaZ;
			if (mirorIn) {
				float f3 = x2;
				x2 = x;
				x = f3;
			}

			PositionTextureVertex vertex7 = new PositionTextureVertex( x,  y,  z, 0.0F, 0.0F);
			PositionTextureVertex vertex0 = new PositionTextureVertex(x2,  y,  z, 0.0F, 8.0F);
			PositionTextureVertex vertex1 = new PositionTextureVertex(x2, y2,  z, 8.0F, 8.0F);
			PositionTextureVertex vertex2 = new PositionTextureVertex( x, y2,  z, 8.0F, 0.0F);
			PositionTextureVertex vertex3 = new PositionTextureVertex( x,  y, z2, 0.0F, 0.0F);
			PositionTextureVertex vertex4 = new PositionTextureVertex(x2,  y, z2, 0.0F, 8.0F);
			PositionTextureVertex vertex5 = new PositionTextureVertex(x2, y2, z2, 8.0F, 8.0F);
			PositionTextureVertex vertex6 = new PositionTextureVertex( x, y2, z2, 8.0F, 0.0F);
			float f4 = (float)texOffX;
			float f5 = (float)texOffX + depth;
			float f6 = (float)texOffX + depth + width;
			float f7 = (float)texOffX + depth + width + width;
			float f8 = (float)texOffX + depth + width + depth;
			float f9 = (float)texOffX + depth + width + depth + width;
			float f10 = (float)texOffY;
			float f11 = (float)texOffY + depth;
			float f12 = (float)texOffY + depth + height;
			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex3, vertex7, vertex0}, f5, f10, f6, f11, texWidth, texHeight, mirorIn, Direction.DOWN);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{vertex1, vertex2, vertex6, vertex5}, f6, f11, f7, f10, texWidth, texHeight, mirorIn, Direction.UP);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{vertex7, vertex3, vertex6, vertex2}, f4, f11, f5, f12, texWidth, texHeight, mirorIn, Direction.WEST);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{vertex0, vertex7, vertex2, vertex1}, f5, f11, f6, f12, texWidth, texHeight, mirorIn, Direction.NORTH);
			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex0, vertex1, vertex5}, f6, f11, f8, f12, texWidth, texHeight, mirorIn, Direction.EAST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{vertex3, vertex4, vertex5, vertex6}, f8, f11, f9, f12, texWidth, texHeight, mirorIn, Direction.SOUTH);
		}
	}

	static class PositionTextureVertex {
		public final Vector3f position;
		public final float textureU;
		public final float textureV;

		public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
			this(new Vector3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex setTextureUV(float texU, float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public PositionTextureVertex(Vector3f posIn, float texU, float texV) {
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
		}
	}

	static class TexturedQuad {
		public final PositionTextureVertex[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn) {
			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth - f, v2 / texHeight - f1);
			if (mirrorIn) {
				int i = positionsIn.length;

				for(int j = 0; j < i / 2; ++j) {
					PositionTextureVertex modelrenderer$positiontexturevertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = modelrenderer$positiontexturevertex;
				}
			}

			this.normal = directionIn.toVector3f();
			if (mirrorIn) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}

		}
	}

	public ConduitRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}



	private Vector3f add(IVertexBuilder buf, MatrixStack stack, float offset, int color, float time, Collection<ModelConduit.DirConType> connections) {
		final float size = 0.125F;
		final float off = ((offset * 1.0F) * size);
		final float min = off - (size / 2);

		final int a = (color >> 24) & 255;
		final int r = (color >> 16) & 255;
		final int g = (color >>  8) & 255;
		final int b = (color >>  0) & 255;

		Matrix4f matrix = stack.getLast().getMatrix();

		ModelBox middle = new ModelBox(min, min, min, size, size, size);

		ModelBox[][] tubes = new ModelBox[6][];

		/*for (DirConType dirConType : connections) {
			List<ModelBox> set = new ArrayList<>();

			Vector3f p = dirConType.dir.toVector3f();
			p.apply(f -> {
				if (f == 0) return min;
				return f;
			});
			Vector3f s = dirConType.dir.toVector3f();
			s.apply(f -> {
				if (f < 0) return 1F + min;
				else if (f > 0) return -1F + min;
				else return size;
			});
			set.add(newBox(p, s));
			class PSet implements Float2FloatFunction {
				final boolean flip;
				PSet(boolean flip) { this.flip = flip; }
				@Override
				public float get(float f) {
					if (f == 0) return min - 0.125F;
					f *= -1; //Because all instances I need the opposite.
					double d = Math.sin((time / 20) * Math.PI);
					if (flip) d *= -1;
					double x = (off - ((f * 1.5) * size)) * f;
					double v = ((((d + 1) / 2) * (x + 1)) - 1);//Credit: Phlosion from Discord.
					return (float) (v * f);
				}
			}
			class SSet implements Float2FloatFunction {
				@Override
				public float get(float f) {
					float l = 0.125F;
					if (f < 0) return l;
					else if (f > 0) return -l;
					else return size + 0.25F;
				}
			}
			ConnectionType type = dirConType.type;
			if (type.extraRender) {
				if (type.input) {
					p = dirConType.dir.toVector3f();
					p.apply(new PSet(false));
					s = dirConType.dir.toVector3f();
					s.apply(new SSet());
					set.add(newBox(p, s));
				}
				if (type.output) {
					p = dirConType.dir.toVector3f();
					p.apply(new PSet(true));
					s = dirConType.dir.toVector3f();
					s.apply(new SSet());
					set.add(newBox(p, s));
				}
			}

			tubes[dirConType.dir.getIndex()] = set.toArray(new ModelBox[0]);
		}*/

		for (Direction direction : Direction.values()) {
			ModelBox[] set = tubes[direction.getIndex()];
			if (set != null) {
				boolean skip = true;
				for (ModelBox tube : set) {
					for (Direction dir2 : Direction.values()) {
						if (!skip || direction != dir2.getOpposite()) {
							TexturedQuad quad = tube.getSide(dir2);
							for (PositionTextureVertex vertexPosition : quad.vertexPositions) {
								Vector3f pos = vertexPosition.position;
								buf.pos(matrix, pos.getX(), pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
							}
						}
					}
					skip = false;
				}
			} else  {
				TexturedQuad quad = middle.getSide(direction);
				for (PositionTextureVertex vertexPosition : quad.vertexPositions) {
					Vector3f pos = vertexPosition.position;
					buf.pos(matrix, pos.getX(), pos.getY(), pos.getZ()).color(r, g, b, a).endVertex();
				}
			}
		}

		return new Vector3f(off, off + (size * 3), off);
	}

	private <Cap> void update(MatrixStack stack, IRenderTypeBuffer typeBuf, int combinedLight, int combinedOverlay, Link<Cap> link) {
		ModelConduit conduit;
		if (CASHE) conduit = MAP.computeIfAbsent(link.getType(), type -> new ModelConduit(type, TIME));
		else conduit = new ModelConduit(link.getType(), TIME);
		int color = link.getType().color;

		final int a = (color >> 0x18) & 0xff;
		final int r = (color >> 0x10) & 0xff;
		final int g = (color >> 0x08) & 0xff;
		final int b = (color >> 0x00) & 0xff;

		conduit.update(link);
		IVertexBuilder buf = typeBuf.getBuffer(conduit.getRenderType(Objects.requireNonNull(link.getType().getRegistryName())));
		conduit.render(stack, buf, combinedLight, combinedOverlay,
				r / 255F,
				g / 255F,
				b / 255F,
				a / 255F
		);
	}



	@Override
	public void render(
			ConduitTE te, float partialTicks,
			MatrixStack stack, IRenderTypeBuffer typeBuf,
			int combinedLight, int combinedOverlay) {
		stack.push();
		stack.translate(0.5, 0.5, 0.5);
		stack.scale(0.5F, 0.5F, 0.5F);

		te.getLinks().values().forEach(link -> update(stack, typeBuf, combinedLight, combinedOverlay, link));

		if (ClientConfig.get().shouldShowLines()) {
			IVertexBuilder buffer = typeBuf.getBuffer(OVERLAY_LINES);
			Matrix4f matrix = stack.getLast().getMatrix();

			for (int x = -1; x <= 1; x += 2)
				for (int z = -1; z <= 1; z += 2)
					for (int y = -1; y <= 1; y += 2)
						buffer.pos(matrix, x, y, z).color(0, 255, 0, 255).endVertex();

			for (int x = -1; x <= 1; x += 2)
				for (int y = -1; y <= 1; y += 2)
					for (int z = -1; z <= 1; z += 2)
						buffer.pos(matrix, x, y, z).color(0, 0, 255, 255).endVertex();

			for (int z = -1; z <= 1; z += 2)
				for (int y = -1; y <= 1; y += 2)
					for (int x = -1; x <= 1; x += 2)
						buffer.pos(matrix, x, y, z).color(255, 0, 0, 255).endVertex();
		}

		stack.pop();
	}

	public void renderOld(
			ConduitTE te, float partialTicks,
			MatrixStack stack, IRenderTypeBuffer typeBuf,
			int combinedLight, int combinedOverlay) {
		final float time;
		ClientWorld world = Minecraft.getInstance().world;
		if (world != null) time = world.getGameTime() + partialTicks;
		else time = 0;

		stack.push();
		IVertexBuilder buf = typeBuf.getBuffer(CONDUIT);
		stack.translate(0.5, 0.5, 0.5);
		//stack.scale(8, 8, 8);

		Map<ConduitType<?>, Link<?>> links = te.getLinks();
		//int size = links.size();
		//AtomicReference<Float> offset = new AtomicReference<>((size / 2F) - 0.5F);
		class IdColor {
			final String id;
			final int color;
			final Vector3f offset;

			IdColor(String id, int color, Vector3f offset) {
				this.id = id;
				this.color = color;
				this.offset = offset;
			}
		}
		List<IdColor> ids = new ArrayList<>();
		final IVertexBuilder finalBuf = buf;
		links.forEach((type, link) -> {
			stack.push();
			ids.add(new IdColor(link.getNetworkID().toString().substring(0, 3), type.color,
					add(finalBuf, stack, ETRegistry.getSortValue(type), type.color, time,
							link.getParts().entrySet().stream().map(entry -> new ModelConduit.DirConType(entry.getKey(),
							entry.getValue().asNode().flatMap(Node::asIO).map(io -> {
								if (io.isBuffer()) return ConnectionType.BUFFER;
								if (io.isInput()) return io.isOutput() ? ConnectionType.INOUT : ConnectionType.INPUT;
								if (io.isOutput()) return ConnectionType.OUTPUT;
								return ConnectionType.DEFAULT;
							}).orElse(ConnectionType.DEFAULT))
					).collect(Collectors.toCollection(ArrayList::new)))));
			//offset.updateAndGet(v -> v + 1);
			stack.pop();
		});

		//ids.add(new IdColor(String.format("%f: %f", time, Math.sin(time)), -1, Vector3f.YP));
		/*add(buf, stack, -1, 0xFFFF0000,
				Arrays.stream(Direction.values())
						.filter(dir -> dir.getAxis() == Direction.Axis.X)
						.collect(Collectors.toList())
		);
		add(buf, stack, 0, 0xFFFFFFFF,
				Arrays.stream(Direction.values())
						.filter(dir -> dir.getAxis() == Direction.Axis.Y)
						.collect(Collectors.toList())
		);
		add(buf, stack, 1, 0xFF0000FF,
				Arrays.stream(Direction.values())
						.filter(dir -> dir.getAxis() == Direction.Axis.Z)
						.collect(Collectors.toList())
		);*/

//		Matrix4f matrix = stack.getLast().getMatrix();

//		buf.pos(matrix, -1, 0, -1).color(255, 255, 255, 255).endVertex();
//		//buf.pos(matrix, 1, 0, 0).color(255, 255, 255, 255).endVertex();
//
//		buf.pos(matrix, 1, 0, -1).color(255, 255, 255, 255).endVertex();
//		//buf.pos(matrix, 1, 0, 1).color(255, 255, 255, 255).endVertex();
//
//		buf.pos(matrix, 1, 0, 1).color(255, 255, 255, 255).endVertex();
//		//buf.pos(matrix, 0, 0, 1).color(255, 255, 255, 255).endVertex();
//
//		buf.pos(matrix, -1, 0, 1).color(255, 255, 255, 255).endVertex();
//		//buf.pos(matrix, 0, 0, 0).color(255, 255, 255, 255).endVertex();

		if (ClientConfig.get().shouldShowLines()) {
			buf = typeBuf.getBuffer(OVERLAY_LINES);
			Matrix4f matrix = stack.getLast().getMatrix();

			for (int x = -1; x <= 1; x += 2)
				for (int z = -1; z <= 1; z += 2)
					for (int y = -1; y <= 1; y += 2)
						buf.pos(matrix, x, y, z).color(0, 255, 0, 255).endVertex();

			for (int x = -1; x <= 1; x += 2)
				for (int y = -1; y <= 1; y += 2)
					for (int z = -1; z <= 1; z += 2)
						buf.pos(matrix, x, y, z).color(0, 0, 255, 255).endVertex();

			for (int z = -1; z <= 1; z += 2)
				for (int y = -1; y <= 1; y += 2)
					for (int x = -1; x <= 1; x += 2)
						buf.pos(matrix, x, y, z).color(255, 0, 0, 255).endVertex();
		}

		float scale = 0.125F / 5F;

		//final int[] y = {0};
		FontRenderer fontRenderer = renderDispatcher.getFontRenderer();
		//RenderSystem.disableDepthTest();
		if (ids.isEmpty()) {
			stack.push();
			stack.scale(-scale, -scale, scale);
			stack.rotate(Vector3f.YP.rotationDegrees(renderDispatcher.renderInfo.getYaw()));
			String s = "ERROR";
			float x = (float) (-fontRenderer.getStringWidth(s) / 2);
			fontRenderer.renderString(
					s,
					x,
					0,
					0xFFFF0000,
					false,
					stack.getLast().getMatrix(),
					typeBuf,
					false,
					0xFF000000,
					combinedLight
			);
			stack.pop();
		} else {
			ids.forEach((id) -> {
				stack.push();
				stack.translate(id.offset.getX(), id.offset.getY(), id.offset.getZ());
				stack.scale(-scale, -scale, scale);
				stack.rotate(Vector3f.YP.rotationDegrees(renderDispatcher.renderInfo.getYaw()));
				stack.rotate(Vector3f.ZP.rotationDegrees(-22.5f));

				String s = id.id;
				//float x = (float) (-fontRenderer.getStringWidth(s) / 2);
				fontRenderer.renderString(
						s,
						0,
						0,
						id.color,
						false,
						stack.getLast().getMatrix(),
						typeBuf,
						false,
						0xFF000000,
						combinedLight
				);
				//y[0] += fontRenderer.FONT_HEIGHT;
				stack.pop();
			});
		}
		//RenderSystem.enableDepthTest();
		stack.pop();
	}
}
