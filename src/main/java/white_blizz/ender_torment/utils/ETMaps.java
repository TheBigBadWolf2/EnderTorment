package white_blizz.ender_torment.utils;

import com.google.common.collect.Iterators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;

public final class ETMaps {
	public static <K, V> HashMap<K, V> newHashMap(
			boolean nullIsEmpty,
			Function<K, V> mapper,
			Iterable<K> keys
	) { return newHashMap(nullIsEmpty, mapper, keys.iterator()); }

	public static <K, V> HashMap<K, V> newHashMap(
			boolean nullIsEmpty,
			Function<K, V> mapper,
			Iterator<K> keys
	) {
		HashMap<K, V> map = new HashMap<>();
		while (keys.hasNext()) {
			K key = keys.next();
			V value = mapper.apply(key);
			if (value != null || !nullIsEmpty) map.put(key, value);
		}
		return map;
	}

	public static <K, V> HashMap<K, V> newHashMap(
			boolean nullIsEmpty,
			Function<K, V> mapper,
			K[] keys
	) { return newHashMap(nullIsEmpty, mapper, Iterators.forArray(keys)); }
}
