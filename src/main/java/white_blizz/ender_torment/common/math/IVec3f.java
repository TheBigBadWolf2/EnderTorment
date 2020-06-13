package white_blizz.ender_torment.common.math;

public interface IVec3f {
	float getX();
	float getY();
	float getZ();

	default StaticVec3f toStatic() { return new StaticVec3f(this); }
}
