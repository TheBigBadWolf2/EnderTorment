package white_blizz.ender_torment.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.*;

public class ETWorldUtils {
	@Nullable
	public static World getWorld(DimensionType dim) {
		return DistExecutor.runForDist(
				() -> () -> {
					if (EffectiveSide.get().isServer())
						return ServerLifecycleHooks.getCurrentServer().getWorld(dim);
					World world = Minecraft.getInstance().world;
					if (world != null && world.getDimension().getType() == dim) return world;
					return null;
				},
				() -> () -> ServerLifecycleHooks.getCurrentServer().getWorld(dim)
		);
	}

	private interface LightFunc extends
			ToIntBiFunction<ILightReader, BlockPos>,
			BiFunction<ILightReader, BlockPos, Integer> {
		int getLight(ILightReader world, BlockPos pos);
		@Override default Integer apply(ILightReader world, BlockPos pos) { return getLight(world, pos); }
		@Override default int applyAsInt(ILightReader world, BlockPos pos) { return getLight(world, pos); }
	}

	private interface LightFuncs extends BiFunction<ILightReader, BlockPos, int[]> {
		int[] getLights(ILightReader world, BlockPos pos);

		@Override default int[] apply(ILightReader world, BlockPos pos) { return getLights(world, pos); }
	}

	public enum LightCombo implements LightFuncs {
		BLOCK((world, pos) -> world.getLightFor(LightType.BLOCK, pos)),
		SKY((world, pos) -> world.getLightFor(LightType.SKY, pos)),
		BOTH((world, pos) -> world.getLightFor(LightType.BLOCK, pos),
				(world, pos) -> world.getLightFor(LightType.SKY, pos));

		private final LightFunc[] funcs;
		LightCombo(LightFunc... funcs) { this.funcs = funcs; }

		@Override
		public int[] getLights(ILightReader world, BlockPos pos) {
			return Arrays.stream(funcs)
					.mapToInt(func -> func.getLight(world, pos))
					.toArray();
		}
	}

	private interface CombineFunc extends
			Function<int[], Integer>,
			ToIntFunction<int[]> {
		int combine(int[] values);
		@Override default Integer apply(int[] ints) { return combine(ints); }
		@Override default int applyAsInt(int[] value) { return combine(value); }
	}

	public enum CombineFunction implements CombineFunc {
		FLOOR_AVERAGE(values -> MathHelper.floor(Arrays.stream(values).average().orElse(0))),
		CEIL_AVERAGE(values -> MathHelper.ceil(Arrays.stream(values).average().orElse(0))),
		MIN(values -> Arrays.stream(values).min().orElse(0)),
		MAX(values -> Arrays.stream(values).max().orElse(0)),
		SUM(values -> Arrays.stream(values).sum())
		;

		private final CombineFunc func;
		CombineFunction(CombineFunc func) { this.func = func; }

		@Override public int combine(int[] values) { return func.combine(values); }
	}

	public static int getLight(ILightReader world, BlockPos pos,
							   LightCombo combo, CombineFunction func) {
		return func.combine(combo.getLights(world, pos));
	}
	public static int getLightAround(ILightReader world, BlockPos pos, LightCombo combo,
									 CombineFunction blockFunc, CombineFunc blocksFunc) {
		return blocksFunc.combine(
				Arrays.stream(Direction.values())
						.map(pos::offset)
						.mapToInt(p -> getLight(world, p, combo, blockFunc))
						.toArray()
		);
	}

	public static int getHeight(IWorldReader world, Heightmap.Type type, double x, double z) {
		int x1 = MathHelper.floor(x - 0.5);
		int x2 = MathHelper.ceil(x - 0.5);
		int z1 = MathHelper.floor(z - 0.5);
		int z2 = MathHelper.ceil(z - 0.5);

		IntBinaryOperator getHeight = (_x, _z) -> world.getChunk(_x >> 4, _z >> 4).getTopBlockY(type, _x & 15, _z & 15) + 1;

		int h1 = getHeight.applyAsInt(x1, z1);
		int h2 = getHeight.applyAsInt(x1, z2);
		int h3 = getHeight.applyAsInt(x2, z2);
		int h4 = getHeight.applyAsInt(x2, z1);

		IntBinaryOperator sel = Math::max;
		return sel.applyAsInt(
				sel.applyAsInt(h1, h2),
				sel.applyAsInt(h3, h4)
		);
	}
}