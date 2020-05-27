package white_blizz.ender_torment.common.ender_flux;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import white_blizz.ender_torment.common.CommonConfig;
import white_blizz.ender_torment.utils.IEnchantmentList;

import java.util.function.BooleanSupplier;

public interface IEnderFluxStorage
		extends INBTSerializable<CompoundNBT> {
	/**
	 * The default value for decay.
	 */
	//double DEFAULT_DECAY = 0.00005;
	static boolean isDecayEnabled() {
		return CommonConfig.get().decay_enabled.get();
	}
	static double getDefaultDecayRate() {
		if (isDecayEnabled())
			return CommonConfig.get().default_decay_rate.get();
		return 0D;
	}
	@FunctionalInterface
	interface Factory<T> {
		T make(int capacity, int maxReceive, int maxExtract, int flux, double decay);
	}

	@SuppressWarnings("PointlessBitwiseExpression")
	class Builder<T> {
		private static final int INPUT = 1 << 0;
		private static final int OUTPUT = 1 << 1;

		private final Factory<T> factory;
		private int flux = 0, capacity = -1, maxReceive = -1, maxExtract = -1;
		private double decay = getDefaultDecayRate();
		private int flags = INPUT | OUTPUT;

		public Builder(Factory<T> factory) { this.factory = factory; }

		public Builder<T> capacity(int capacity) {
			if (capacity < 0) capacity = -1;
			this.capacity = capacity;
			if (maxReceive < 0 && (flags & INPUT) == INPUT)
				maxReceive = capacity;
			if (maxExtract < 0 && (flags & OUTPUT) == OUTPUT)
				maxExtract = capacity;
			return this;
		}

		public Builder<T> input(int input) {
			if (capacity < 0) capacity = maxReceive = input;
			else maxReceive = MathHelper.clamp(input, 0, capacity);
			return this;
		}
		public Builder<T> inputOnly() {
			flags |= INPUT;
			flags &= ~OUTPUT;
			return this;
		}
		public Builder<T> inputOnly(int input) { return inputOnly().input(input); }

		public Builder<T> output(int output) {
			if (capacity < 0) capacity = maxExtract = output;
			else maxExtract = MathHelper.clamp(output, 0, capacity);
			return this;
		}
		public Builder<T> outputOnly() {
			flags |= OUTPUT;
			flags &= ~INPUT;
			return this;
		}
		public Builder<T> outputOnly(int output) { return outputOnly().output(output); }

		public Builder<T> io(int io) {
			if (capacity < 0) capacity = io;
			maxExtract = maxReceive = io;
			return this;
		}
		public Builder<T> biDirectional() {
			flags |= INPUT | OUTPUT;
			return this;
		}
		public Builder<T> biDirectional(int io) { return biDirectional().io(io); }

		public Builder<T> decay(double decay) {
			this.decay = MathHelper.clamp(decay, 0, 1);
			return this;
		}

		public Builder<T> flux(int flux) {
			if (capacity < 0) return capacity(this.flux = flux);
			this.flux = MathHelper.clamp(flux, 0, capacity);
			return this;
		}

		public Builder<T> resetCapacity() {
			capacity = -1;
			return this;
		}
		public Builder<T> resetInput(boolean flag) {
			if (flag) flags |= INPUT;
			maxReceive = -1;
			return this;
		}
		public Builder<T> resetOutput(boolean flag) {
			if (flag) flags |= OUTPUT;
			maxExtract = -1;
			return this;
		}
		public Builder<T> resetIO(boolean flagI, boolean flagO) { return resetInput(flagI).resetOutput(flagO); }
		public Builder<T> resetIO(boolean flag) { return resetIO(flag, flag); }
		public Builder<T> reset(boolean flagI, boolean flagO) { return resetCapacity().resetIO(flagI, flagO); }
		public Builder<T> reset(boolean flag) { return resetCapacity().resetIO(flag); }

		public T build() {
			int in, out;
			if (capacity < 0)
				throw new IllegalStateException("capacity not set");
			if (maxReceive < 0 && (flags & INPUT) == INPUT)
				throw new IllegalStateException("maxReceive not set");
			else if ((flags & INPUT) == INPUT)
				in = Math.min(maxReceive, capacity);
			else in = 0;
			if (maxExtract < 0 && (flags & OUTPUT) == OUTPUT)
				throw new IllegalStateException("maxExtract not set");
			else if ((flags & OUTPUT) == OUTPUT)
				out = Math.min(maxExtract, capacity);
			else out = 0;

			return factory.make(
					capacity,
					in, out,
					MathHelper.clamp(flux, 0, capacity),
					isDecayEnabled() ? MathHelper.clamp(decay, 0, 1) : 0D
			);
		}
	}

	static <T> Builder<T> New(Factory<T> factory) { return new Builder<>(factory); }

	//int charge(int amount, boolean simulate);

	int receiveEnderFlux(int maxReceive, boolean simulate);
	int extractEnderFlux(int maxExtract, boolean simulate);

	int getMaxReceive();
	int getMaxExtract();

	int getEnderFluxStored();
	int getMaxEnderFluxStored();

	double getDecayRate();

	/**
	 * Returns if this storage can have ender flux extracted.
	 * If this is false, then any calls to extractEnderFlux will return 0.
	 */
	boolean canExtractFlux();

	/**
	 * Used to determine if this storage can receive ender flux.
	 * If this is false, then any calls to receiveEnderFlux will return 0.
	 */
	boolean canReceiveFlux();

	interface IEnderFluxTickResult {}

	enum TickResult implements IEnderFluxTickResult, BooleanSupplier {
		TRUE(true), FALSE(false);

		private final boolean value;

		TickResult(boolean b) { value = b; }
		@Override public boolean getAsBoolean() { return value; }
	}

	/**
	 * Handles removing flux lost from decay.
	 * @return if any changes were made.
	 */
	default boolean tick() { return false; }

	default IEnderFluxTickResult tickWithResult() {
		if (tick()) return TickResult.TRUE;
		else return TickResult.FALSE;
	}

	void setEnchantmentList(IEnchantmentList list);
}