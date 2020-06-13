package white_blizz.ender_torment.common.math;

public class LockingMutVec3f extends ModCountTracker implements ILockingMutVec3f {

	private final Lock x, y, z;

	public LockingMutVec3f(Lock x, Lock y, Lock z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void setX(float x) {
		this.x.set(x);
		mod();
	}

	@Override
	public void setY(float y) {
		this.y.set(y);
		mod();
	}

	@Override
	public void setZ(float z) {
		this.z.set(z);
		mod();
	}

	@Override
	public ILockingMutVec3f withLock(Lock.Type type, Lock.Axis axis, Lock.ILockData data) {
		Lock x = this.x;
		Lock y = this.y;
		Lock z = this.z;
		switch (axis) {
			case X: x = type.make(this.x.get(), data); break;
			case Y: y = type.make(this.y.get(), data); break;
			case Z: z = type.make(this.z.get(), data); break;
		}
		return new LockingMutVec3f(x, y, z);
	}

	@Override public float getX() { return x.get(); }
	@Override public float getY() { return y.get(); }
	@Override public float getZ() { return z.get(); }
}
