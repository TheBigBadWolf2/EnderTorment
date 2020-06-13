package white_blizz.ender_torment.common.state;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DirectionalProperty extends Property<Integer> {
	private final ImmutableSet<Integer> allowedValues;
	private final boolean canHaveNone;

	public DirectionalProperty(String name, boolean canHaveNone) {
		super(name, Integer.class);
		this.canHaveNone = canHaveNone;
		Set<Integer> set = Sets.newHashSet();

		for(int i = canHaveNone ? -1 : 0; i < 6; ++i) {
			set.add(i);
		}

		this.allowedValues = ImmutableSet.copyOf(set);
	}

	@Override
	public Collection<Integer> getAllowedValues() {
		return allowedValues;
	}

	@Override
	public Optional<Integer> parseValue(String value) {
		if (value.equals("none")) {
			if (canHaveNone) return Optional.of(-1);
			else return Optional.empty();
		}
		Direction direction = Direction.byName(value);
		if (direction == null) return Optional.empty();
		return Optional.of(direction.getIndex());
	}

	public @Nullable Direction get(IStateHolder<?> state) {
		return decode(state.get(this));
	}

	public <C extends IStateHolder<C>> C with(C state, @Nullable Direction dir) {
		return state.with(this, encode(dir));
	}

	public @Nullable Direction decode(int value) {
		if (value == -1) return null;
		return Direction.byIndex(value);
	}

	public int encode(@Nullable Direction dir) {
		if (dir == null) return -1;
		return dir.getIndex();
	}

	@Override
	public String getName(Integer value) {
		if (canHaveNone && value == -1) return "none";
		return Direction.byIndex(value).getName();
	}
}
