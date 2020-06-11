package white_blizz.ender_torment.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import white_blizz.ender_torment.client.render.model.ETModel;
import white_blizz.ender_torment.client.render.model.ETModelRenderer;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.conduit.Link;
import white_blizz.ender_torment.common.conduit.Node;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static white_blizz.ender_torment.client.render.ModelConduit.ConnectionFlag.*;

@ParametersAreNonnullByDefault
public class ModelConduit extends ETModel {

	private static final float length = 32F;
	private static final float hLength = length / 2F;
	private static final float size = 2f;
	private static final float hSize = size / 2F;

	final ETModelRenderer main;
	final ETModelRenderer[][] tubeSets = new ETModelRenderer[6][4];

	public enum ConnectionFlag {
		I(1),
		O(2),
		B(3);

		private final int index;
		ConnectionFlag(int index) { this.index = index; }
	}

	public enum ConnectionType {
		DEFAULT(),
		INPUT (I),
		BUFFER(B),
		OUTPUT(O),
		INOUT (I, O);

		private static final ConnectionType[] VALUES = values();
		private static final Map<ConnectionFlag, List<ConnectionType>> flagLists;
		static {
			//noinspection UnstableApiUsage
			flagLists = Arrays.stream(VALUES)
					.flatMap(value -> Arrays.stream(value.flags)
							.map(flag -> new Tuple<>(flag, value))
					)
					/*.reduce(new HashMap<ConnectionFlag, List<ConnectionType>>(),
							(map, tup) -> {
								map.compute(tup.getA(), (k, v) -> {
									if (v == null) v = new ArrayList<>();
									v.add(tup.getB());
									return v;
								});
								return map;
							},
							(a, b) -> {
								b.forEach((k, v) -> a.merge(k, v, (al, bl) -> {
									al.addAll(bl);
									return al;
								}));
								return a;
							}
					)
					.entrySet()
					.stream()*/
					.collect(ImmutableMap.toImmutableMap(
							Tuple::getA,
							tup -> ImmutableList.of(tup.getB()),
							(a, b) -> Streams.concat(a.stream(), b.stream()).distinct().collect(ImmutableList.toImmutableList())
					))
			;
		}

		private final ConnectionFlag[] flags;
		ConnectionType(ConnectionFlag... flags) { this.flags = flags; }
		public boolean hasExtraRenders() { return flags.length > 0; }

		public ConnectionType or(ConnectionType or) {
			return this;
		}

		public static ConnectionType fromFlags(ConnectionType _default, ConnectionFlag... flags) {
			flagLists.entrySet()
					.stream()
					.filter(entry -> Arrays.stream(flags).anyMatch(flag -> flag == entry.getKey()))
					.map(Map.Entry::getValue)
					.reduce((a, b) -> {
						List<ConnectionType> c = new ArrayList<>(a);
						c.retainAll(b);
						return c;
					})
					.filter(list -> list.size() > 0)
					;
			return _default;
		}
	}

	public static class DirConType {
		final Direction dir;
		final ConnectionType type;

		public DirConType(Direction dir, ConnectionType type) {
			this.dir = dir;
			this.type = type;
		}

		public int index() { return dir.getIndex(); }
		public boolean hasExtraRenders() { return type.hasExtraRenders(); }
		public IntStream indexes() { return Arrays.stream(type.flags).mapToInt(flag -> flag.index); }
	}

	private static ETModelRenderer newBox(ETModelRenderer renderer, Vector3f pos, Vector3f size) {
		return newBox(renderer, pos, size, false);
	}

	private static ETModelRenderer newBox(ETModelRenderer renderer, Vector3f pos, Vector3f size, boolean type2) {
		//pos.add(-renderer.rotationPointX, -renderer.rotationPointY, -renderer.rotationPointZ);
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
		if (type2) return renderer.addBox2(x, y, z, w, h, d);
		return renderer.addBox(x, y, z, w, h, d);
	}
	private final List<FloatConsumer> timeUpdaters;

	ModelConduit(ConduitType<?> type, float time) { this(type);update(time); }
	public ModelConduit(ConduitType<?> type) { this(ETRegistry.getSortValue(type)); }
	private ModelConduit(float offset) {
		super((rl) -> ETRenderType.CONDUIT);
		textureSize = (int)length;

		final float off = ((offset * 1.0F) * size);
		final float min = off - hSize;
		final float max = off + hSize;

		main = new ETModelRenderer(this);
		main.setTextureOffset(-off);
		main.addBox(min, min, min, size, size, size);
		List<FloatConsumer> list = new ArrayList<>();

		final double Pi2 = Math.PI * 2;
		final double cycle = Math.PI / 20;
		Arrays.stream(Direction.values()).forEach(dir -> {
			ETModelRenderer side = new ETModelRenderer(this);
			side.setRotationPoint(off, off, off);
			Vector3f p = dir.toVector3f();
			p.apply(f -> {
				if (f == 0) return -hSize;
				return f * hSize;
			});
			Vector3f s = dir.toVector3f();
			s.apply(f -> {
				if (f < 0) return -hLength - min;
				else if (f > 0) return hLength - max;
				else return size;
			});
			main.addChild(side);
			tubeSets[dir.getIndex()][0] = newBox(side, p, s);


			Float2FloatFunction pSet = f -> {
				if (f == 0) return size;
				return f * hSize;
			}, sSet = f -> {
				if (f < 0) return -size;
				else if (f > 0) return size;
				else return -size * 2F;
			};


			ETModelRenderer input = new ETModelRenderer(this);
			input.mirror = dir.getAxisDirection().getOffset() < 0;
			//input.showModel = false;
			p = dir.toVector3f();
			p.apply(pSet);
			s = dir.toVector3f();
			s.apply(sSet);
			side.addChild(input);
			tubeSets[dir.getIndex()][1] = newBox(input, p, s, true);


			ETModelRenderer output = new ETModelRenderer(this);
			output.mirror = dir.getAxisDirection().getOffset() < 0;
			output.showModel = false;
			p = dir.toVector3f();
			/*p.apply(f -> {
				if (f < 0) return -16F - off + size;
				else if (f > 0) return 16F - off - size;
				else return size;
			});*/
			p.apply(pSet);
			s = dir.toVector3f();
			s.apply(sSet);
			side.addChild(output);
			tubeSets[dir.getIndex()][2] = newBox(output, p, s, true);


			ETModelRenderer buffer = new ETModelRenderer(this);
			buffer.mirror = dir.getAxisDirection().getOffset() < 0;
			p = dir.toVector3f();
			p.apply(f -> {
				if (f < 0) return -(hLength / 2F) - (off - hSize) / 2;
				if (f > 0) return (hLength / 2F) - (off + hSize) / 2;
				return size;
			});
			s = dir.toVector3f();
			s.apply(sSet);
			side.addChild(buffer);
			tubeSets[dir.getIndex()][3] = newBox(buffer, p, s, true);


			list.add(time -> {
				class PSet implements Float2FloatFunction {
					final boolean flip;
					private PSet(boolean flip) { this.flip = flip; }
					@Override
					public float get(float f) {
						if (f == 0) return 0;
						double d = Math.sin(time * cycle);
						if (flip) d *= -1;
						double x = (-off - ((f * 1.5) * size)) * f / hLength;
						double v = ((d + 1) / 2) * (x + 1);
						return (float) (v * f) * hLength;
					}
				}
				Vector3f vec = dir.toVector3f();
				vec.apply(new PSet(false));
				input.setRotationPoint(vec.getX(), vec.getY(), vec.getZ());
				vec = dir.toVector3f();
				vec.apply(new PSet(true));
				output.setRotationPoint(vec.getX(), vec.getY(), vec.getZ());
				if (true) {
					vec = dir.toVector3f();
					//if (dir.getAxis() == Direction.Axis.Y) vec.mul(-1);
					vec.apply(f -> {
						if (f == 0) return 0;
						return (float) ((f * time * cycle) % (Pi2));
					});
					buffer.setAngles(vec);
				} else buffer.setAngles(new Vector3f());
			});
		});
		timeUpdaters = ImmutableList.copyOf(list);
	}

	void update(float time) { timeUpdaters.forEach(updater -> updater.accept(time)); }

	public <Cap> void update(Link<Cap> link) {
		/*Arrays.stream(Direction.values())
				.map(Direction::getIndex)
				.map(i -> tubeSets[i])
				.filter(Objects::nonNull)
				.flatMap(Arrays::stream)
				.filter(Objects::nonNull)
				.forEach(tube -> tube.showModel = false);*/
		main.hideChildren();
		link.getParts()
				.entrySet()
				.stream()
				.map(entry -> new ModelConduit.DirConType(entry.getKey(),
						entry.getValue().asNode().flatMap(Node::asIO).map(io -> {
							if (io.isBuffer()) return ConnectionType.BUFFER;
							if (io.isInput()) return io.isOutput() ? ConnectionType.INOUT : ConnectionType.INPUT;
							if (io.isOutput()) return ConnectionType.OUTPUT;
							return ConnectionType.DEFAULT;
						}).orElse(ConnectionType.DEFAULT))
				)
				.forEach(dirConType -> {
					ETModelRenderer[] tubeSet = tubeSets[dirConType.index()];
					tubeSet[0].showModel = true;
					//tubeSet[3].showModel = true;
					if (dirConType.hasExtraRenders()) dirConType.indexes()
							.mapToObj(index -> tubeSet[index])
							.filter(Objects::nonNull)
							.forEach(tube -> tube.showModel = true);

				});
				/*.keySet()
				.stream()
				.map(Direction::getIndex)
				.map(i -> tubeSets[i])
				.filter(Objects::nonNull)
				.map(set -> set[0])
				.filter(Objects::nonNull)
				.forEach(tube -> tube.showModel = true);*/
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
