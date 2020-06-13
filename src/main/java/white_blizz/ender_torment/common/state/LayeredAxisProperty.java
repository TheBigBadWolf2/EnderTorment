package white_blizz.ender_torment.common.state;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayeredAxisProperty extends Property<Integer> {
	//logbase2(a-b)
	//or ln(a-b)/ln(2)
	//and ceil
	private final ImmutableSet<Integer> allowedValues;
	private final int range, size, mask;
	private final int min, defaultValue, max;

	public LayeredAxisProperty(String name, int min, int max) {
		this(name, min, max, min);
	}
	public LayeredAxisProperty(String name, int min, int max, int defaultValue) {
		super(name, Integer.class);
		if (min < 0) {
			throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
		} else if (max <= min) {
			throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
		} else {
			this.min = min;
			this.max = max;
			this.defaultValue= defaultValue;
			Set<Integer> set = Sets.newHashSet();
			range = (max - min) + 1;
			size = MathHelper.log2DeBruijn(range);
			int mask = 0;
			for (int i = 0; i < size; i++) {
				mask <<= 1;
				mask |= 1;
			}
			this.mask = mask;

			for(int n = min; n <= max; ++n) {
				for (int p = min; p <= max; p++) {
					int s = n + p;
					if (min <= s && s <= max) {
						int a = (n - min) | ((p - min) << size);
						set.add(a);
					}
				}
			}

			this.allowedValues = ImmutableSet.copyOf(set);
		}
	}

	public int getSize() { return size; }
	public int getMin() { return min; }
	public int getMax() { return max; }

	public class LayeredAxis {
		@Nullable private final Integer negative, positive;

		public LayeredAxis(@Nullable Integer negative, @Nullable Integer positive) {
			this.negative = negative;
			this.positive = positive;
		}

		public Optional<Integer> getNegative() {
			return Optional.ofNullable(negative);
		}

		public Optional<Integer> getPositive() {
			return Optional.ofNullable(positive);
		}

		public IntStream stream() {
			IntStream.Builder builder = IntStream.builder();
			if (negative != null) builder.accept(negative);
			if (positive != null) builder.accept(positive);
			return builder.build();
		}

		@Contract("null -> false")
		private boolean isValid(@Nullable Integer i) {
			return i != null && min <= i && i <= max;
		}

		public boolean isValid() {
			return isValid(negative) && isValid(positive);
		}

		public Optional<Integer> combined() {
			if (isValid(negative) && isValid(positive)) {
				return combine(negative, positive);
			} return Optional.empty();
		}

		public Optional<Integer> get(Direction.AxisDirection dir) {
			if (dir == Direction.AxisDirection.NEGATIVE) return getNegative();
			if (dir == Direction.AxisDirection.POSITIVE) return getNegative();
			return Optional.empty();
		}

		public int getOrDefault(Direction.AxisDirection dir) {
			return get(dir).orElse(defaultValue);
		}

		public boolean isFull() {
			if (isValid(positive) && isValid(negative)) {
				return positive + negative == max;
			}
			return false;
		}
	}

	public Optional<Integer> combine(int n, int p) {
		if (min <= n && n <= max && min <= p && p <= max) {
			int v = (n - min) | ((p - min) << size);
			if (allowedValues.contains(v)) return Optional.of(v);
		}
		return Optional.empty();
	}

	public Optional<Integer> negative(int combined) {
		if (allowedValues.contains(combined)) return Optional.of((combined & mask) + min);
		return Optional.empty();
	}

	public Optional<Integer> positive(int combined) {
		if (allowedValues.contains(combined)) return Optional.of(((combined >> size) & mask) + min);
		return Optional.empty();
	}

	public LayeredAxis split(int combined) {
		return new LayeredAxis(negative(combined).orElse(null), positive(combined).orElse(null));
	}

	public LayeredAxis get(IStateHolder<?> state) {
		return split(state.get(this));
	}

	public <C extends IStateHolder<C>> C with(C state, Direction.AxisDirection dir, int value) {
		if (min <= value && value <= max) {
			LayeredAxis layeredAxis = get(state);

			int n = layeredAxis.getNegative().orElse(defaultValue);
			int p = layeredAxis.getPositive().orElse(defaultValue);

			if (dir == Direction.AxisDirection.NEGATIVE) n = value;
			else if (dir == Direction.AxisDirection.POSITIVE) p = value;

			return this.combine(n, p).map(c -> state.with(this, c)).orElse(state);
		}
		return state;
	}

	@Override
	public Collection<Integer> getAllowedValues() {
		return allowedValues;
	}

	private int parse(String value) {
		if ("?".equals(value)) return defaultValue;
		return Integer.parseInt(value);
	}

	@Override
	public Optional<Integer> parseValue(String value) {
		try {
			String[] split = value.split("_");
			int n = parse(split[1]);
			int p = parse(split[0]);
			return combine(n, p);
		} catch (NumberFormatException | IndexOutOfBoundsException var3) {
			return Optional.empty();
		}
	}

	@Override
	public String getName(Integer value) {
		LayeredAxis split = split(value);
		String n = split.getNegative().map(String::valueOf).orElse("?");
		String p = split.getPositive().map(String::valueOf).orElse("?");
		return String.format(
				"%s_%s",
				p, n
		);
	}
}
