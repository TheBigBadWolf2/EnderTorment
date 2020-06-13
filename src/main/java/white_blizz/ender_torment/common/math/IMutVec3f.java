package white_blizz.ender_torment.common.math;

public interface IMutVec3f extends IVec3f {
	void setX(float x);
	void setY(float y);
	void setZ(float z);

	default ILockingMutVec3f withLock(Lock.Type type, Lock.Axis axis) {
		return withLock(type, axis, new Lock.ILockData() {});
	}
	ILockingMutVec3f withLock(Lock.Type type, Lock.Axis axis, Lock.ILockData data);
}
