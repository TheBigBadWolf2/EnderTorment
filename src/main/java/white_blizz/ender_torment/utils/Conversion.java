package white_blizz.ender_torment.utils;

import net.minecraft.util.math.MathHelper;

import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

public class Conversion {
	public static class Ratio {
		private final int in, out;

		public Ratio(int in, int out) {
			this.in = in;
			this.out = out;
		}

		public int getIn() {
			if (out == 0) return 0;
			return in;
		}
		public int getOut() {
			if (in == 0) return 0;
			return out;
		}
		public boolean isZero() { return in == 0 || out == 0; }
	}

	public static class Container implements IConversionContainer {
		private final IntSupplier amount, capacity, maxTrans;

		public Container(IntSupplier amount) {
			this(amount, () -> Integer.MAX_VALUE);
		}
		public Container(IntSupplier amount, IntSupplier capacity) {
			this(amount, capacity, capacity);
		}

		public Container(IntSupplier amount, IntSupplier capacity, IntSupplier maxTrans) {
			this.amount = amount;
			this.capacity = capacity;
			this.maxTrans = maxTrans;
		}

		public int getAmount() { return amount.getAsInt(); }
		public int getCapacity() { return capacity.getAsInt(); }
		public int getMaxTrans() { return maxTrans.getAsInt(); }
	}

	private final int ratioIn, ratioOut, rate;
	private IntUnaryOperator modIn = IntUnaryOperator.identity(),
			modOut = IntUnaryOperator.identity(),
			modRate = IntUnaryOperator.identity();


	public Conversion(int ratioIn, int ratioOut, int rate) {
		this.ratioIn = ratioIn;
		this.ratioOut = ratioOut;
		this.rate = rate;
	}

	public void setMods(IntUnaryOperator modIn,
						IntUnaryOperator modOut,
						IntUnaryOperator modRate) {
		if (modIn != null) this.modIn = modIn;
		if (modOut != null) this.modOut = modOut;
		if (modRate != null) this.modRate = modRate;
	}

	public int getModdedRatioIn() { return modIn.applyAsInt(ratioIn); }
	public int getModdedRatioOut() { return modOut.applyAsInt(ratioOut); }
	public int getModdedRate() { return modRate.applyAsInt(rate); }

	public int inputRate() { return getModdedRatioIn() * getModdedRate(); }
	public int outputRate() { return getModdedRatioOut() * getModdedRate(); }

	public Ratio min(int input, int output) {
		int ratioIn = getModdedRatioIn();
		int ratioOut = getModdedRatioOut();

		int i = input * ratioOut;
		int o = output * ratioIn;
		int io = Math.min(i, o);
		i = io / ratioOut;
		o = io / ratioIn;
		if (i == 0 || o == 0) return new Ratio(0, 0);
		return new Ratio(i, o);
	}

	public Ratio calculate(
			IConversionContainer inputBuffer,
			IConversionContainer outputBuffer
	) {
		int input = Math.min(inputBuffer.getAmount(), Math.min(inputRate(), inputBuffer.getMaxTrans()));
		int output = Math.min(outputBuffer.getCapacity() - outputBuffer.getAmount(), Math.min(outputRate(), outputBuffer.getMaxTrans()));

		return min(input, output);
	}
}
