package white_blizz.ender_torment.common.math;

import net.minecraft.util.math.MathHelper;

public interface Lock {
	enum Axis {
		X, Y, Z
	}

	enum Type {
		FREE{
			@Override
			public Lock make(float value, ILockData data) {
				return new Free(value);
			}
		}, SLIDING {
			@Override
			public Lock make(float value, ILockData data) {
				return new Sliding(data.getMin(), data.getMax(), value);
			}
		}, STATIC {
			@Override
			public Lock make(float value, ILockData data) {
				return new Lock.Static(value);
			}
		};

		public abstract Lock make(float value, ILockData data);
	}

	interface ILockData {
		default float getMin() { return 0; }
		default float getMax() { return 1; }
	}

	float get();
	void set(float v);
	Type getType();

	class Free implements Lock {
		private float value;

		public Free(float value) { this.value = value; }

		@Override public float get() { return value; }
		@Override public void set(float v) { value = v; }
		@Override public Type getType() { return Type.FREE; }
	}

	class Sliding implements Lock {
		private final float min, max;
		private float value;

		public Sliding(float min, float max, float value) {
			this.min = min;
			this.max = max;
			this.value = value;
		}

		@Override public float get() { return value; }
		@Override public void set(float v) { value = MathHelper.clamp(v, min, max); }
		@Override public Type getType() { return Type.SLIDING; }
	}

	class Static implements Lock {
		private final float value;

		public Static(float value) { this.value = value; }

		@Override public float get() { return value; }
		@Override public void set(float v) { }
		@Override public Type getType() { return Type.STATIC; }
	}
}
