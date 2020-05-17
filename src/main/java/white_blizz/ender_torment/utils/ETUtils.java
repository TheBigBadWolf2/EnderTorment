package white_blizz.ender_torment.utils;

import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ETUtils {
	public static Vec3d center(BlockPos pos) {
		return new Vec3d(pos).add(0.5, 0.5, 0.5);
	}

	public static Optional<TileEntity> getTileEntity(
			IBlockReader world, BlockPos pos
	) {
		if (world == null) return Optional.empty();
		return Optional.ofNullable(world.getTileEntity(pos));
	}

	public static <I, O> LazyOptional<O> mapToLazy(
			Optional<I> opt, Function<I, LazyOptional<O>> mapper) {
		return opt.map(mapper).orElseGet(LazyOptional::empty);
	}

	public static <C> LazyOptional<C> getCap(IBlockReader world, BlockPos pos, Capability<C> cap) {
		return mapToLazy(getTileEntity(world, pos), te -> te.getCapability(cap));
	}

	public static <C> LazyOptional<C> getCap(IBlockReader world, BlockPos pos, Capability<C> cap, Direction side) {
		return mapToLazy(getTileEntity(world, pos), te -> te.getCapability(cap, side));
	}

	public static Tuple<Integer, Double> split(double value) {
		int i = MathHelper.floor(value);
		double d = value % 1;
		return new Tuple<>(i, d);
	}

	public static void d1(IWorld world, double range, Vec3d center) {
		RedstoneParticleData data = new RedstoneParticleData(
				1F, 1F, 1F, 1F);
		for (int i = 0; i < 360; i++) {
			float rads = (float) Math.toRadians(i);
			for (double j = range; j > 0; j--) {
				Vec3d vec = new Vec3d(j, 0, 0)
						.rotateYaw(rads)
						.add(center);
				world.addParticle(data,
						vec.x, vec.y, vec.z,
						0, 0, 0
				);
			}
		}
	}

	public static void d2(IWorld world, double range, Vec3d center) {
		int iRange = MathHelper.ceil(range);
		for (int x = -iRange; x <= iRange; x++) {
			for (int z = -iRange; z <= iRange; z++) {
				for (int y = -iRange; y <= iRange; y++) {
					Vec3d vec = new Vec3d(x, y, z).add(center);
					RedstoneParticleData data = new RedstoneParticleData(
							(float) Math.abs(x) / iRange,
							(float) Math.abs(y) / iRange,
							(float) Math.abs(z) / iRange,
							1F);
					world.addParticle(data,
							vec.x, vec.y, vec.z,
							-x, -y, -z
					);
				}
			}
		}
	}
}
