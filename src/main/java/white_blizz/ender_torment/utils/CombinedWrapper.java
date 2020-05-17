package white_blizz.ender_torment.utils;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public abstract class CombinedWrapper<Cap, Me extends Cap> {
	/*
	 * format: Func_{RETURN}_{PARAMS}
	 */
	@FunctionalInterface protected interface Func_I_IB<Cap> { int exe(Cap cap, int i, boolean b); }
	@FunctionalInterface protected interface Func_I<Cap> { int exe(Cap cap); }
	@FunctionalInterface protected interface Func_D<Cap> { double exe(Cap cap); }
	@FunctionalInterface protected interface Func_B<Cap> { boolean exe(Cap cap); }
	@FunctionalInterface protected interface Func_V_O<Cap, T1> { void exe(Cap cap, T1 t1); }

	protected final Cap[] caps;

	/**
	 * Should return self.
	 */
	protected abstract Me me();

	@SafeVarargs protected CombinedWrapper(Cap... caps) { this.caps = caps; }

	private Stream<Cap> stream() { return Arrays.stream(caps); }

	/*
	 * format: exe_{RETURN}_{PARAMS}_{FLAGS}
	 * Types:
	 *	I: int
	 *	B: boolean
	 *	O: Object
	 * Flags:
	 *	Sb: Substitute, replace next input with output.
	 *	Mx: Max, return the max value.
	 *	Mn: Min, return the min value.
	 *	Av: Average, return the average.
	 *	Sm: Sum, add it up.
	 *	Ad: And, and them.
	 *	Or: Or, or them.
	 */

	protected final int exe_I_IB_Sb(Func_I_IB<Cap> func, int i, boolean b) {
		for (Cap cap : caps) i = func.exe(cap, i, b);
		return i;
	}

	protected final int exe_I_Mx(Func_I<Cap> func, int _default) {
		return stream().mapToInt(func::exe).max().orElse(_default);
	}

	protected final int exe_I_Sm(Func_I<Cap> func) {
		return stream().mapToInt(func::exe).sum();
	}

	protected final double exe_D_Av(Func_D<Cap> func, double _default) {
		return stream().mapToDouble(func::exe).average().orElse(_default);
	}

	protected final boolean exe_B_Or(Func_B<Cap> func, boolean _default) {
		return stream().map(func::exe).reduce((a, b) -> a || b).orElse(_default);
	}

	protected final <T1> void exe_V_O(Func_V_O<Cap, T1> func, T1 t1) {
		stream().forEach(cap -> func.exe(cap, t1));
	}

	@SafeVarargs
	private static <T> T[] makeArray(int length, T... a) {
		return Arrays.copyOf(a, length);
	}

	@SafeVarargs
	public static <Cap, To extends CombinedWrapper<Cap, ? extends Cap>, From> To convert(
			Function<Cap[], To> factory, Function<From, Cap> cast,
			Cap[] a,
			From... listIn
	) {
		Cap[] listOut = Arrays.copyOf(a, listIn.length);//makeArray(listIn.length);//
		for (int i = 0; i < listIn.length; i++) listOut[i] = cast.apply(listIn[i]);
		return factory.apply(listOut);
	}

	public static <Cap, To extends CombinedWrapper<Cap, ? extends Cap>, From> To convert(
			Function<Cap[], To> factory, Function<From, Cap[]> expand,
			From in
	) {
		return factory.apply(expand.apply(in));
	}

	public static <Cap> Function<? extends IItemHandler, Cap[]> fromItems(
			Capability<Cap> cap,
			Cap[] a
	) {
		return items -> {
			List<Cap> list = new ArrayList<>();
			for (int i = 0; i < items.getSlots(); i++) {
				items.getStackInSlot(i).getCapability(cap).ifPresent(list::add);
			}
			return list.toArray(a);
		};
	}
}
