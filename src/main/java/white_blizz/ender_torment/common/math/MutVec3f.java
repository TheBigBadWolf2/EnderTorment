package white_blizz.ender_torment.common.math;

public class MutVec3f extends ModCountTracker implements IMutVec3f {
	private float x;
	private float y;
	private float z;

	public MutVec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override public float getX() { return x; }
	@Override public float getY() { return y; }
	@Override public float getZ() { return z; }

	public MutVec3f(float[] values) { set(values); }
	public void set(float[] values) {
		this.x = values[0];
		this.y = values[1];
		this.z = values[2];
		mod();
	}
	public void setX(float x) { this.x = x; mod(); }
	public void setY(float y) { this.y = y; mod(); }
	public void setZ(float z) { this.z = z; mod(); }

	@Override
	public ILockingMutVec3f withLock(Lock.Type type, Lock.Axis axis, Lock.ILockData data) {
		Lock x = new Lock.Free(this.x);
		Lock y = new Lock.Free(this.y);
		Lock z = new Lock.Free(this.z);
		switch (axis) {
			case X: x = type.make(this.x, data); break;
			case Y: y = type.make(this.y, data); break;
			case Z: z = type.make(this.z, data); break;
		}
		return new LockingMutVec3f(x, y, z);
	}


}
