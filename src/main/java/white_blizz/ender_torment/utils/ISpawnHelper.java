package white_blizz.ender_torment.utils;

import net.minecraft.entity.Entity;

import java.util.function.Consumer;

@FunctionalInterface
public interface ISpawnHelper<T extends Entity> {
	Consumer<T> getPre();
	default Consumer<T> getPost() { return null; }

	default void pre(T entity) {
		Consumer<T> pre = getPre();
		if (pre != null) pre.accept(entity);
	}

	default void post(T entity) {
		Consumer<T> post = getPost();
		if (post != null) post.accept(entity);
	}
}
