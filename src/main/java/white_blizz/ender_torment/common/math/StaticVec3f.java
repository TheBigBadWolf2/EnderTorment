package white_blizz.ender_torment.common.math;

import net.minecraft.util.math.MathHelper;

public class StaticVec3f implements IVec3f {
	private final float x;
	private final float y;
	private final float z;

	public StaticVec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public StaticVec3f(IVec3f vec) {
		this(vec.getX(), vec.getY(), vec.getZ());
	}

	@Override public float getX() { return x; }
	@Override public float getY() { return y; }
	@Override public float getZ() { return z; }


	public StaticVec3f cross(IVec3f vec) {
		float x1 = this.x;
		float y1 = this.y;
		float z1 = this.z;
		float x2 = vec.getX();
		float y2 = vec.getY();
		float z2 = vec.getZ();
		return new StaticVec3f(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2);
	}


	public StaticVec3f sub(IVec3f other) {
		return new StaticVec3f(x - other.getX(), y - other.getY(), z - other.getZ());
	}
	public StaticVec3f add(IVec3f other) {
		return new StaticVec3f(x + other.getX(), y + other.getY(), z + other.getZ());
	}
	public StaticVec3f mul(IVec3f other) {
		return new StaticVec3f(x * other.getX(), y * other.getY(), z * other.getZ());
	}
	public StaticVec3f mulNaNCheck(IVec3f other) {
		float x = this.x * other.getX();
		float y = this.y * other.getY();
		float z = this.z * other.getZ();
		if (Float.isNaN(x)) x = 0;
		if (Float.isNaN(y)) y = 0;
		if (Float.isNaN(z)) z = 0;
		return new StaticVec3f(x, y, z);
	}

	public StaticVec3f div(IVec3f other) {
		return new StaticVec3f(x / other.getX(), y / other.getY(), z / other.getZ());
	}

	public float length() {
		return MathHelper.sqrt(x * x + y * y + z * z);
	}

	@Override
	public StaticVec3f toStatic() { return this; }

	public StaticVec3f normalized() {
		float f = this.x * this.x + this.y * this.y + this.z * this.z;
		if ((double)f < 1.0E-5D) {
			return this;
		} else {
			float f1 = MathHelper.fastInvSqrt(f);
			float x = this.x * f1;
			float y = this.y * f1;
			float z = this.z * f1;
			return new StaticVec3f(x, y, z);
		}
	}

	public StaticVec3f lerp(IVec3f vec, float pct) {
		float f = 1.0F - pct;
		return new StaticVec3f(
			this.x * f + vec.getX() * pct,
			this.y * f + vec.getY() * pct,
			this.z * f + vec.getZ() * pct
		);
	}
}
