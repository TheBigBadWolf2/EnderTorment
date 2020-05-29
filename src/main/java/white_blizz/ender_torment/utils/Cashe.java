package white_blizz.ender_torment.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Cashe<T> {
	private final Supplier<World> worldSupplier;
	private World world;
	private final BlockPos pos;
	private final BiFunction<World, BlockPos, T> getter;
	//private final Comparator<T> areSame;
	private final Predicate<T> validator;
	@Nullable
	private T t;


	public Cashe(
			Supplier<World> worldSupplier, BlockPos pos,
			BiFunction<World, BlockPos, T> getter,
			BiPredicate<T, T> areEqual) {
		this(worldSupplier, pos, getter, t1 -> {
			World world = worldSupplier.get();
			if (world == null) return false;
			T t2 = getter.apply(world, pos);
			if (t2 == null) return false;
			return !areEqual.test(t1, t2);
		});
	}

	public Cashe(
			Supplier<World> worldSupplier, BlockPos pos,
			BiFunction<World, BlockPos, T> getter,
			Predicate<T> validator) {
		this.worldSupplier = worldSupplier;
		this.pos = pos;
		this.getter = getter;
		this.validator = validator;
		world = worldSupplier.get();
		if (world != null) t = getter.apply(world, pos);
	}

	public boolean hasChanged() {
		if (world == null) {
			world = worldSupplier.get();
			if (world == null) return false;
			t = getter.apply(world, pos);
		}
		if (t == null) return true;
		return validator.test(t);
	}
}
