package white_blizz.ender_torment.client.render.model;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/*
 *    ----
 *    |ge|
 *    |ab|
 * ---+--+------
 * |ga|ab|be|eg|
 * |hd|dc|cf|fh|
 * ---+--+------
 *    |dc|
 *    |hf|
 *    ----
/*  01234
 * 0 ge
 * 1gabeg
 * 2hdcfh
 * 3 hf
 */
@SuppressWarnings("SpellCheckingInspection")
public class ETModelBox extends ETModelBoxBase {
	private final ETTexturedQuadBase[] quads;
	public final float posX1;
	public final float posY1;
	public final float posZ1;
	public final float posX2;
	public final float posY2;
	public final float posZ2;

	public ETModelBox(float texOffX, float texOffY, float x, float y, float z, float width, float height, float depth, boolean mirorIn, float texSize) {
		this.posX1 = x;
		this.posX2 = x + width;
		this.posY1 = y;
		this.posY2 = y + height;
		this.posZ1 = z;
		this.posZ2 = z + depth;
		/*if (width < 0) {
			this.posX1 = x + width;
			this.posX2 = x;
		} else {
			this.posX1 = x;
			this.posX2 = x + width;
		}

		if (height < 0) {
			this.posY1 = y + height;
			this.posY2 = y;
		} else {
			this.posY1 = y;
			this.posY2 = y + height;
		}

		if (depth < 0) {
			this.posZ1 = z + depth;
			this.posZ2 = z;
		} else {
			this.posZ1 = z;
			this.posZ2 = z + depth;
		}*/

		this.quads = new ETTexturedQuadBase[6];

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

		PositionTextureVertexA a = new PositionTextureVertexA();
		PositionTextureVertexA b = new PositionTextureVertexA();
		PositionTextureVertexA c = new PositionTextureVertexA();
		PositionTextureVertexA d = new PositionTextureVertexA();
		PositionTextureVertexA e = new PositionTextureVertexA();
		PositionTextureVertexA f = new PositionTextureVertexA();
		PositionTextureVertexA g = new PositionTextureVertexA();
		PositionTextureVertexA h = new PositionTextureVertexA();

		final int[][] uvMap = {
				{0, 0},
				{1, 0},
				{1, 1},
				{0, 1}
		};

		class Face {
			final PositionTextureVertexB[] verts;
			final boolean negative;
			Direction dir;
			Face(int u, int v, boolean negative, PositionTextureVertexA... verts) {
				this.negative = negative;
				this.verts = new PositionTextureVertexB[4];
				for (int i = 0, l = verts.length; i < l; i++) {
					this.verts[i] = verts[i].setTextureUV(
							us[u+uvMap[i][0]] / texSize,
							vs[v+uvMap[i][1]] / texSize
					);
				}

			}

			void setDir(Direction.Axis axis) {
				Direction.AxisDirection axDir;
				if (negative) axDir = Direction.AxisDirection.NEGATIVE;
				else axDir = Direction.AxisDirection.POSITIVE;
				dir = Direction.getFacingFromAxisDirection(axis, axDir);
				if (mirorIn) dir = dir.getOpposite();
				axDir = dir.getAxisDirection();
				Vector3f vec = dir.toVector3f();
				vec.mul(axDir.getOffset());
				if (axDir.getOffset() < 0) vec.mul(posX1, posY1, posZ1);
				else vec.mul(posX2, posY2, posZ2);
				for (PositionTextureVertexB vert : verts) vert.getPosition().add(vec);
			}

			void makeQuad() {
				int i = dir.getIndex();
				quads[i] = new TexturedQuad(verts, mirorIn, dir);
			}
		}
		Direction.Axis a1 = min.axises[0],
				a2 = max.axises[1],
				a3 = max.axises[0];

		Face[] faces = new Face[6];
		boolean n1 = true,
				n2 = a1 != Direction.Axis.Z,
				n3 = a1 != Direction.Axis.X,
				fp = false;
		/*  01234U
		 * 0 ge
		 * 1gabeg
		 * 2hdcfh
		 * 3 hf
		 * V
		 */
		Face front  = faces[0] = new Face(1, 1,  n1, a, b, c, d);
		Face back   = faces[1] = new Face(3, 1, !n1, e, g, h, f);
		Face top    = faces[2] = new Face(1, 0,  n2, g, e, b, a);
		Face bottom = faces[3] = new Face(1, 2, !n2, d, c, f, h);
		Face left   = faces[4] = new Face(0, 1,  n3, g, a, d, h);
		Face right  = faces[5] = new Face(2, 1, !n3, b, e, f, c);

		if (fp) {
			Direction.Axis t = a3;
			a3 = a2;
			a2 = t;
		}

		front.setDir(a1);
		back.setDir(a1);

		top.setDir(a2);
		bottom.setDir(a2);

		left.setDir(a3);
		right.setDir(a3);

		for (Face face : faces) face.makeQuad();
	}


	private static class PositionTextureVertexA extends ETPositionTextureVertexBase {
		public final Vector3f position;
		private static class UV {
			private final float u, v;
			private UV(float u, float v) { this.u = u; this.v = v; }
			private UV(ETPositionTextureVertexBase base) { this(base.u(), base.v()); }
		}
		private final List<UV> uvs = new ArrayList<>();

		public PositionTextureVertexA() { this(new Vector3f()); }
		public PositionTextureVertexA(Vector3f position) { this.position = position; }
		private PositionTextureVertexA(Vector3f position, UV... uvs) {
			this(position);
			Collections.addAll(this.uvs, uvs);
		}

		@Override Vector3f getPosition() { return position; }
		@Override float u() { return 0; }
		@Override float v() { return 0; }

		public ETPositionTextureVertexBase guess(int pos, ETPositionTextureVertexBase[] quad) {
			UV uv = null;
			if (uvs.size() == 1) uv = uvs.get(0);
			else {
				ETPositionTextureVertexBase sV, sU;
				Supplier<Stream<UV>> ssV, ssU;
				//01
				//32
				if (pos == 0) {
					sV = quad[1];
					sU = quad[3];
				} else if (pos == 1) {
					sV = quad[0];
					sU = quad[2];
				} else if (pos == 2) {
					sV = quad[3];
					sU = quad[1];
				} else if (pos == 3) {
					sV = quad[2];
					sU = quad[0];
				} else throw new IndexOutOfBoundsException(String.valueOf(pos));
				Stream<UV> uvStream = stream();

				if (sV instanceof PositionTextureVertexA) ssV = ((PositionTextureVertexA) sV)::stream;
				else ssV = () -> Stream.of(new UV(sV));
				if (sU instanceof PositionTextureVertexA) ssU = ((PositionTextureVertexA) sU)::stream;
				else ssU = () -> Stream.of(new UV(sU));

				/*if (sV instanceof PositionTextureVertexA) {
					final PositionTextureVertexA sUa = (PositionTextureVertexA) sV;
					uvStream = uvStream.filter(uv0 -> sUa.stream().anyMatch(uv1 -> uv1.u == uv0.u));
					if (lU) uvStream = uvStream.filter(uv0 -> sUa.stream().anyMatch(uv1 -> uv1.v > uv0.v));
					else uvStream = uvStream.filter(uv0 -> sUa.stream().anyMatch(uv1 -> uv1.v < uv0.v));
				} else if (lU) uvStream = uvStream.filter(uv0 -> uv0.u == sV.u()).filter(uv0 -> sV.v() > uv0.v);
				else uvStream = uvStream.filter(uv0 -> uv0.u == sV.u()).filter(uv0 -> sV.v() < uv0.v);*/

				uvStream = uvStream.filter(uv0 -> ssV.get().anyMatch(uv1 -> uv1.v == uv0.v));
				//if (lU) uvStream = uvStream.filter(uv0 -> ssV.get().anyMatch(uv1 -> uv1.v > uv0.v));
				//else uvStream = uvStream.filter(uv0 -> ssV.get().anyMatch(uv1 -> uv1.v < uv0.v));

				uvStream = uvStream.filter(uv0 -> ssU.get().anyMatch(uv1 -> uv1.u == uv0.u));
				//if (lV) uvStream = uvStream.filter(uv0 -> ssU.get().anyMatch(uv1 -> uv1.u > uv0.u));
				//else uvStream = uvStream.filter(uv0 -> ssU.get().anyMatch(uv1 -> uv1.u < uv0.u));

				UV[] uvs = uvStream.toArray(UV[]::new);

				if (uvs.length == 1) uv = uvs[0];
				else if (uvs.length > 1) return new PositionTextureVertexA(position, uvs);
			}
			if (uv != null) return new PositionTextureVertexB(position, uv.u, uv.v);
			throw new IllegalStateException();
		}

		private Stream<UV> stream() { return uvs.stream(); }

		@Override
		public PositionTextureVertexB setTextureUV(float texU, float texV) {
			/*if (uvs.stream().noneMatch(uv -> uv.u == texU && uv.v == texV))
				uvs.add(new UV(texU, texV));
			return this;*/
			return new PositionTextureVertexB(position, texU, texV);
		}
	}

	private static class PositionTextureVertexB extends ETPositionTextureVertexBase {
		public final Vector3f position;
		public final float texU, texV;

		public PositionTextureVertexB(Vector3f position, float texU, float texV) {
			this.position = position;
			this.texU = texU;
			this.texV = texV;
		}

		@Override Vector3f getPosition() { return position; }
		@Override float u() { return texU; }
		@Override float v() { return texV; }

		@Override
		public PositionTextureVertexB setTextureUV(float texU, float texV) {
			return new PositionTextureVertexB(position, texU, texV);
		}
	}

	private static class TexturedQuad extends ETTexturedQuadBase {
		private static PositionTextureVertexB[] guess(PositionTextureVertexA[] in) {
			ETPositionTextureVertexBase[] tests = in;
			ETPositionTextureVertexBase[] out = new ETPositionTextureVertexBase[4];

			boolean done;
			do {
				for (int i = 0; i < out.length; i++) {
					ETPositionTextureVertexBase test = tests[i];
					if (test instanceof PositionTextureVertexA)
						out[i] = ((PositionTextureVertexA) test).guess(i, tests);
				}
				tests = out;
				done = true;
				for (ETPositionTextureVertexBase base : out) {
					if (base instanceof PositionTextureVertexA) {
						done = false;
						break;
					}
				}
			} while (!done);

			return Arrays.stream(out)
					.map(base -> (PositionTextureVertexB)base)
					.toArray(PositionTextureVertexB[]::new);
		}

		public final PositionTextureVertexB[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad(PositionTextureVertexB[] positionsIn, boolean mirrorIn, Direction directionIn) {
			this.vertexPositions = positionsIn;
			this.normal = directionIn.toVector3f();
			if (mirrorIn) {
				int i = positionsIn.length;

				for (int j = 0; j < i / 2; ++j) {
					PositionTextureVertexB vertex = positionsIn[j];
					positionsIn[j] = positionsIn[i - 1 - j];
					positionsIn[i - 1 - j] = vertex;
				}
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}
		}

		@Override ETPositionTextureVertexBase[] getVertexes() { return vertexPositions; }
		@Override Vector3f getNormal() { return normal; }
	}

	@Override ETTexturedQuadBase[] getQuads() { return quads; }
}
