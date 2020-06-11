package white_blizz.ender_torment.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import white_blizz.ender_torment.utils.ETUtils;

import java.util.Arrays;
import java.util.Random;

public class ETModelRenderer {
	private static final float SCALE = 16F;

	private float textureSize = 64.0F;
	private float textureOffset;
	private float textureOffsetX;
	private float textureOffsetY;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotateAngleX;
	public float rotateAngleY;
	public float rotateAngleZ;
	public boolean mirror;
	public boolean showModel = true;
	private ETModelRenderer parent;
	private final ObjectList<ETModelBoxBase> cubeList = new ObjectArrayList<>();
	private final ObjectList<ETModelRenderer> childModels = new ObjectArrayList<>();

	public ETModelRenderer(ETModel model) {
		model.accept(this);
		this.setTextureSize(model.textureSize);
	}

	public ETModelRenderer(ETModel model, int texOff) {
		this(model.textureSize, texOff);
		model.accept(this);
	}
	public ETModelRenderer(ETModel model, int texOffX, int texOffY) {
		this(model.textureSize, texOffX, texOffY);
		model.accept(this);
	}

	public ETModelRenderer(int textureSize, int textureOffset) {
		this.setTextureSize(textureSize).setTextureOffset(textureOffset);
	}

	public ETModelRenderer(int textureSize, int textureOffsetX, int textureOffsetY) {
		this.setTextureOffset(textureOffsetX, textureOffsetY).setTextureSize(textureSize);
	}

	public void copyModelAngles(ETModelRenderer modelRendererIn) {
		this.rotateAngleX = modelRendererIn.rotateAngleX;
		this.rotateAngleY = modelRendererIn.rotateAngleY;
		this.rotateAngleZ = modelRendererIn.rotateAngleZ;
		this.rotationPointX = modelRendererIn.rotationPointX;
		this.rotationPointY = modelRendererIn.rotationPointY;
		this.rotationPointZ = modelRendererIn.rotationPointZ;
	}

	public void setAngles(Vector3f angles) {
		rotateAngleX = angles.getX();
		rotateAngleY = angles.getY();
		rotateAngleZ = angles.getZ();
	}

	public void hideChildren() {
		childModels.forEach(ETModelRenderer::hideAll);
	}

	public void hideAll() {
		this.showModel = false;
		childModels.forEach(ETModelRenderer::hideAll);
	}

	public void addChild(ETModelRenderer renderer) {
		addChild(renderer, true);
	}

	public void addChild(ETModelRenderer renderer, boolean setParent) {
		if (setParent) {
			if (renderer.parent != null) renderer.parent.childModels.remove(renderer);
			renderer.parent = this;
		}
		this.childModels.add(renderer);
	}

	public ETModelRenderer setTextureOffset(float off) {
		this.textureOffset = off;
		return this;
	}

	public ETModelRenderer setTextureOffset(float offX, float offY) {
		this.textureOffsetX = offX;
		this.textureOffsetY = offY;
		return this;
	}

	private float getTextureOffset() {
		float offset = textureOffset;
		if (parent != null) offset += parent.getTextureOffset();
		return offset;
	}

	public ETModelRenderer addBox(String partName, float x, float y, float z, int width, int height, int depth, float delta, int tex) {
		this.setTextureOffset(tex).addBox(this.getTextureOffset(), x, y, z, (float) width, (float) height, (float) depth, delta, delta, delta, this.mirror, false);
		return this;
	}

	public ETModelRenderer addBox(float x, float y, float z, float width, float height, float depth) {
		this.addBox(this.getTextureOffset(), x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, this.mirror, false);
		return this;
	}

	public ETModelRenderer addBox2(float x, float y, float z, float width, float height, float depth) {
		this.addBox2(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, this.mirror);
		return this;
	}

	public ETModelRenderer addBox(float x, float y, float z, float width, float height, float depth, boolean mirrorIn) {
		this.addBox(this.getTextureOffset(), x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, mirrorIn, false);
		return this;
	}

	public void addBox(float x, float y, float z, float width, float height, float depth, float delta) {
		this.addBox(this.getTextureOffset(), x, y, z, width, height, depth, delta, delta, delta, this.mirror, false);
	}

	public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ) {
		this.addBox(this.getTextureOffset(), x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror, false);
	}

	public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirrorIn) {
		this.addBox(this.getTextureOffset(), x, y, z, width, height, depth, delta, delta, delta, mirrorIn, false);
	}

	private Vector3f getOffset() {
		Vector3f off = new Vector3f(rotationPointX, rotationPointY, rotationPointZ);
		if (parent != null) off.add(parent.getOffset());
		return off;
	}

	private void addBox(float texOff, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, boolean type2) {
		if (!type2)
			this.cubeList.add(new ModelBoxType1(texOff, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, getOffset(), mirorIn, this.textureSize));
		else
			this.cubeList.add(new ETModelBox(0, 0, x, y, z, width, height, depth, mirorIn, this.textureSize));
	}

	private void addBox2(float texOffX, float texOffY, float x, float y, float z, float width, float height, float depth, boolean mirorIn) {
		this.cubeList.add(new ETModelBox(texOffX, texOffY, x, y, z, width, height, depth, mirorIn, this.textureSize));
	}

	public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
		this.rotationPointX = rotationPointXIn;
		this.rotationPointY = rotationPointYIn;
		this.rotationPointZ = rotationPointZIn;
	}

	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
		this.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if (this.showModel) {
			if (!this.cubeList.isEmpty() || !this.childModels.isEmpty()) {
				matrixStackIn.push();
				this.translateRotate(matrixStackIn);
				this.doRender(matrixStackIn.getLast(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

				for (ETModelRenderer modelrenderer : this.childModels) {
					modelrenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
				}

				matrixStackIn.pop();
			}
		}
	}

	public void translateRotate(MatrixStack matrixStackIn) {
		matrixStackIn.translate((double) (this.rotationPointX / SCALE), (double) (this.rotationPointY / SCALE), (double) (this.rotationPointZ / SCALE));
		if (this.rotateAngleZ != 0.0F) {
			matrixStackIn.rotate(Vector3f.ZP.rotation(this.rotateAngleZ));
		}

		if (this.rotateAngleY != 0.0F) {
			matrixStackIn.rotate(Vector3f.YP.rotation(this.rotateAngleY));
		}

		if (this.rotateAngleX != 0.0F) {
			matrixStackIn.rotate(Vector3f.XP.rotation(this.rotateAngleX));
		}

	}

	private void doRender(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixEntryIn.getMatrix();
		Matrix3f matrix3f = matrixEntryIn.getNormal();

		for (ETModelBoxBase box : this.cubeList) {
			for (ETTexturedQuadBase quad : box.getQuads()) {
				Vector3f vector3f = quad.getNormal().copy();
				vector3f.transform(matrix3f);
				float nX = vector3f.getX();
				float nY = vector3f.getY();
				float nZ = vector3f.getZ();

				for (int i = 0; i < 4; ++i) {
					ETPositionTextureVertexBase vertex = quad.getVertexes()[i];
					float x = vertex.getPosition().getX() / SCALE;
					float y = vertex.getPosition().getY() / SCALE;
					float z = vertex.getPosition().getZ() / SCALE;
					Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
					vector4f.transform(matrix4f);
					bufferIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, vertex.u(), vertex.v(), packedOverlayIn, packedLightIn, nX, nY, nZ);
				}
			}
		}

	}

	/**
	 * Returns the model renderer with the new texture parameters.
	 */
	public ETModelRenderer setTextureSize(int textureSizeIn) {
		this.textureSize = (float) textureSizeIn;
		return this;
	}

	public ETModelBoxBase getRandomCube(Random randomIn) {
		return this.cubeList.get(randomIn.nextInt(this.cubeList.size()));
	}

	public static class ModelBoxType1 extends ETModelBoxBase {
		private final TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		public ModelBoxType1(float texOff, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, Vector3f offsets, boolean mirorIn, float texSize) {
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

			float texOff0 = texOff + (texSize / 2);

			float xuv1 = texOff0 + x + offsets.getX();
			float xuv2 = texOff0 + x2 + offsets.getX();
			float yuv1 = texOff0 + y + offsets.getY();
			float yuv2 = texOff0 + y2 + offsets.getY();
			float zuv1 = texOff0 + z + offsets.getZ();
			float zuv2 = texOff0 + z2 + offsets.getZ();

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

			PositionTextureVertex vertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex vertex0 = new PositionTextureVertex(x2, y, z, 0.0F, 8.0F);
			PositionTextureVertex vertex1 = new PositionTextureVertex(x2, y2, z, 8.0F, 8.0F);
			PositionTextureVertex vertex2 = new PositionTextureVertex(x, y2, z, 8.0F, 0.0F);
			PositionTextureVertex vertex3 = new PositionTextureVertex(x, y, z2, 0.0F, 0.0F);
			PositionTextureVertex vertex4 = new PositionTextureVertex(x2, y, z2, 0.0F, 8.0F);
			PositionTextureVertex vertex5 = new PositionTextureVertex(x2, y2, z2, 8.0F, 8.0F);
			PositionTextureVertex vertex6 = new PositionTextureVertex(x, y2, z2, 8.0F, 0.0F);


			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex3, vertex7, vertex0}, xuv1, zuv2, xuv2, zuv1, texSize, mirorIn, Direction.DOWN);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{vertex1, vertex2, vertex6, vertex5}, xuv1, zuv1, xuv2, zuv2, texSize, mirorIn, Direction.UP);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{vertex7, vertex3, vertex6, vertex2}, zuv2, yuv1, zuv1, yuv2, texSize, mirorIn, Direction.WEST);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{vertex0, vertex7, vertex2, vertex1}, xuv1, yuv1, xuv2, yuv2, texSize, mirorIn, Direction.NORTH);
			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{vertex4, vertex0, vertex1, vertex5}, zuv1, yuv1, zuv2, yuv2, texSize, mirorIn, Direction.EAST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{vertex3, vertex4, vertex5, vertex6}, xuv2, yuv1, xuv1, yuv2, texSize, mirorIn, Direction.SOUTH);
		}

		@Override
		TexturedQuad[] getQuads() {
			return quads;
		}
	}



	public static class ModelBoxType2 extends ETModelBoxBase {
		private final TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		public ModelBoxType2(float texOffX, float texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, Vector3f offsets, boolean mirorIn, float texSize) {
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

			class M {
				final Direction.Axis[] axises;
				final float value;

				M(float value, Direction.Axis... axises) {
					this.axises = axises;
					this.value = value;
				}

				M or(M o) {
					Direction.Axis[] axises = new Direction.Axis[this.axises.length+o.axises.length];
					int i = 0;
					for (int j = 0; j < this.axises.length; j++, i++) axises[i] = this.axises[j];
					for (int j = 0; j < o.axises.length; j++, i++) axises[i] = o.axises[j];
					return new M(value, axises);
				}
			}

			M[] ms = {
					new M(width, Direction.Axis.X),
					new M(height, Direction.Axis.Y),
					new M(depth, Direction.Axis.Z)
			};

			M max = Arrays.stream(ms)
					.reduce((a, b) -> {
						if (a.value > b.value) return a;
						if (b.value > a.value) return b;
						if (a.value == b.value) return a.or(b);
						throw new IllegalArgumentException();
					}).get();

			M min = Arrays.stream(ms)
					.reduce((a, b) -> {
						if (a.value < b.value) return a;
						if (b.value < a.value) return b;
						if (a.value == b.value) return a.or(b);
						throw new IllegalArgumentException();
					}).get();

			assert max.axises.length == 2 && min.axises.length == 1;

			x = x - deltaX;
			y = y - deltaY;
			z = z - deltaZ;
			x2 = x2 + deltaX;
			y2 = y2 + deltaY;
			z2 = z2 + deltaZ;

			ETPositionTextureVertexBase vertex7 = new PositionTextureVertex( x,  y,  z, 0.0F, 0.0F);
			ETPositionTextureVertexBase vertex0 = new PositionTextureVertex(x2,  y,  z, 0.0F, 8.0F);
			ETPositionTextureVertexBase vertex1 = new PositionTextureVertex(x2, y2,  z, 8.0F, 8.0F);
			ETPositionTextureVertexBase vertex2 = new PositionTextureVertex( x, y2,  z, 8.0F, 0.0F);
			ETPositionTextureVertexBase vertex3 = new PositionTextureVertex( x,  y, z2, 0.0F, 0.0F);
			ETPositionTextureVertexBase vertex4 = new PositionTextureVertex(x2,  y, z2, 0.0F, 8.0F);
			ETPositionTextureVertexBase vertex5 = new PositionTextureVertex(x2, y2, z2, 8.0F, 8.0F);
			ETPositionTextureVertexBase vertex6 = new PositionTextureVertex( x, y2, z2, 8.0F, 0.0F);

			float[] us = {
					texOffX,
					texOffX + min.value,
					texOffX + min.value + max.value,
					texOffX + min.value + max.value + min.value,
					texOffX + min.value + max.value + min.value + max.value,
			};

			float[] vs = {
					texOffY,
					texOffY + min.value,
					texOffY + min.value + max.value,
					texOffY + min.value + max.value + min.value,
			};

			ETPositionTextureVertexBase[][] vertexes = {
					{vertex4, vertex0, vertex1, vertex5},
					{vertex7, vertex3, vertex6, vertex2},
					{vertex4, vertex3, vertex7, vertex0},
					{vertex1, vertex2, vertex6, vertex5},
					{vertex0, vertex7, vertex2, vertex1},
					{vertex3, vertex4, vertex5, vertex6},
			};



			int[] map = {
					2, 3,
					4, 5,
					1, 0
			};

			class Face {
				final float u1, v1, u2, v2;
				final boolean negative;
				Direction dir;
				Rotation rot = Rotation.NONE;

				Face(int u, int v, boolean negative) {
					u1 = us[u];
					u2 = us[u + 1];
					v1 = vs[v];
					v2 = vs[v + 1];
					this.negative = negative;
				}

				void setDir(Direction.Axis axis) {
					Direction.AxisDirection axDir;
					if (negative) axDir = Direction.AxisDirection.NEGATIVE;
					else axDir = Direction.AxisDirection.POSITIVE;
					dir = Direction.getFacingFromAxisDirection(axis, axDir);
					if (mirorIn) dir = dir.getOpposite();
				}

				void rotate(Rotation by) { rot = rot.add(by); }

				void makeQuad() {
					int i = map[dir.getIndex()];
					int rotate;
					if (rot == Rotation.CLOCKWISE_90) rotate = 1;
					else if (rot == Rotation.CLOCKWISE_180) rotate = 2;
					else if (rot == Rotation.COUNTERCLOCKWISE_90) rotate = 3;
					else rotate = 0;
					quads[i] = new TexturedQuad(
							ETUtils.shift(vertexes[i], rotate),
							u1, v1, u2, v2,
							texSize, false, dir
					);
				}
			}

			Face front = new Face(1, 1, true), back = new Face(3, 1, false);
			Face top = new Face(1, 0, true), bottom = new Face(1, 2, false);
			Face left = new Face(2, 1, true), right = new Face(0, 1, false);

			Direction.Axis a = min.axises[0],
					b = max.axises[1],
					c = max.axises[0];

			front.setDir(a);
			back.setDir(a);

			top.setDir(b);
			bottom.setDir(b);

			left.setDir(c);
			right.setDir(c);

			Face[] faces = {
					front, back,
					top, bottom,
					left, right
			};

			for (Face face : faces) face.makeQuad();

			/*this.quads[0] = new TexturedQuad(vertexes[0], zuv1, yuv1, zuv2, yuv2, texSize, false, Direction.EAST);
			this.quads[1] = new TexturedQuad(vertexes[1], zuv2, yuv1, zuv1, yuv2, texSize, false, Direction.WEST);
			this.quads[2] = new TexturedQuad(vertexes[2], xuv1, zuv2, xuv2, zuv1, texSize, false, Direction.DOWN);
			this.quads[3] = new TexturedQuad(vertexes[3], xuv1, zuv1, xuv2, zuv2, texSize, false, Direction.UP);
			this.quads[4] = new TexturedQuad(vertexes[4], xuv1, yuv1, xuv2, yuv2, texSize, false, Direction.NORTH);
			this.quads[5] = new TexturedQuad(vertexes[5], xuv2, yuv1, xuv1, yuv2, texSize, false, Direction.SOUTH);*/
		}

		@Override
		TexturedQuad[] getQuads() {
			return quads;
		}
	}



	static class PositionTextureVertex extends ETPositionTextureVertexBase {
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

		@Override Vector3f getPosition() { return position; }
		@Override float u() { return textureU; }
		@Override float v() { return textureV; }
	}

	static class PositionTextureVertex2 extends ETPositionTextureVertexBase {
		public final Vector3f position;

		private float textureU;
		private float textureV;

		public PositionTextureVertex2() { position = new Vector3f(); }
		public PositionTextureVertex2(float x, float y, float z) { this(new Vector3f(x, y, z)); }

		public PositionTextureVertex2 setTextureUV(float texU, float texV) {

			return this;
		}



		public PositionTextureVertex2(Vector3f posIn) { this.position = posIn; }

		@Override Vector3f getPosition() { return position; }
		@Override float u() { return 0; }
		@Override float v() { return 0; }
	}

	static class TexturedQuad2 extends ETTexturedQuadBase {
		public final PositionTextureVertex2[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad2(PositionTextureVertex2[] positionsIn, float u1, float v1, float u2, float v2, float texSize, boolean mirrorIn, Direction directionIn) {
			this.vertexPositions = positionsIn;
			float f = 0.0F / texSize;
			float f1 = 0.0F / texSize;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texSize - f, v1 / texSize + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texSize + f, v1 / texSize + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texSize + f, v2 / texSize - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texSize - f, v2 / texSize - f1);
			if (mirrorIn) {
				int i = positionsIn.length;

				for (int j = 0; j < i / 2; ++j) {
					PositionTextureVertex2 vertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = vertex;
				}
			}

			this.normal = directionIn.toVector3f();
			if (mirrorIn) this.normal.mul(-1.0F, 1.0F, 1.0F);

		}

		@Override ETPositionTextureVertexBase[] getVertexes() { return vertexPositions; }
		@Override Vector3f getNormal() { return normal; }
	}

	static class TexturedQuad extends ETTexturedQuadBase {
		public final ETPositionTextureVertexBase[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad(ETPositionTextureVertexBase[] positionsIn, float u1, float v1, float u2, float v2, float texSize, boolean mirrorIn, Direction directionIn) {
			this.vertexPositions = positionsIn;
			float f = 0.0F / texSize;
			float f1 = 0.0F / texSize;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texSize - f, v1 / texSize + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texSize + f, v1 / texSize + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texSize + f, v2 / texSize - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texSize - f, v2 / texSize - f1);
			if (mirrorIn) {
				int i = positionsIn.length;

				for (int j = 0; j < i / 2; ++j) {
					ETPositionTextureVertexBase vertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = vertex;
				}
			}

			this.normal = directionIn.toVector3f();
			if (mirrorIn) this.normal.mul(-1.0F, 1.0F, 1.0F);

		}

		@Override ETPositionTextureVertexBase[] getVertexes() { return vertexPositions; }
		@Override Vector3f getNormal() { return normal; }
	}
}