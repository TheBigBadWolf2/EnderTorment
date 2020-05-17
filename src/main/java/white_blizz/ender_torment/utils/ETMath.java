package white_blizz.ender_torment.utils;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public final class ETMath {
	public static Vec3d nextVec(Random rng,
								double xzRange,
								double minY, double maxY) {
		return nextVec(rng, xzRange, minY, maxY, 1D);
	}
	public static Vec3d nextVec(Random rng,
								double xzRange,
								double minY, double maxY,
								double magnitude) {
		Vec3d vec = new Vec3d(
				MathHelper.nextDouble(rng, -xzRange, xzRange),
				MathHelper.nextDouble(rng, minY, maxY),
				MathHelper.nextDouble(rng, -xzRange, xzRange)
		).normalize();
		if (magnitude != 1D) vec = vec.scale(magnitude);
		return vec;
	}
}
