package white_blizz.ender_torment.common.conduit;

import mcp.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockMap implements Iterable<BlockMap.TypeLink<?>> {
	@Override public Iterator<TypeLink<?>> iterator() { return list.iterator(); }

	public static class TypeLink<Cap> implements Map.Entry<ConduitType<Cap>, Link<Cap>> {
		private final ConduitType<Cap> type;
		private Link<Cap> link;

		private TypeLink(Link<Cap> link) {
			this.type = link.type;
			this.link = link;
		}

		public ConduitType<Cap> getKey() { return type; }

		public Link<Cap> getValue() { return link; }

		public Link<Cap> setValue(Link<Cap> value) {
			Link<Cap> current = this.link;
			this.link = value;
			return current;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TypeLink<?> typeLink = (TypeLink<?>) o;
			return type.equals(typeLink.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}
	}

	private final Set<TypeLink<?>> list = new HashSet<>();

	@Nullable
	public <Cap> Link<Cap> get(ConduitType<Cap> type) {
		for (TypeLink<?> link : list) {
			if (link.type.equals(type))
				return (Link<Cap>) link.link;
		}
		return null;
	}
	public <Cap> void add(Link<Cap> link) {
		list.add(new TypeLink<>(link));

	}
	public <Cap> void remove(Link<Cap> link) {
		Iterator<TypeLink<?>> ittr = list.iterator();
		while (ittr.hasNext()) {
			TypeLink<?> next = ittr.next();
			if (next.link.equals(link)) {
				ittr.remove();
				//next.link.remove(true);
				return;
			}
		}
	}

	public int size() { return list.size(); }
	public boolean isEmpty() { return list.isEmpty(); }


	public boolean containsKey(Object key) {
		return list.stream().anyMatch(entry -> entry.type.equals(key));
	}


	public boolean containsValue(Object value) {
		return list.stream().anyMatch(entry -> entry.link.equals(value));
	}

	public void clear(boolean deleteLinks, boolean dissolveNetworks) {
		if (deleteLinks) list.forEach(typeLink -> {
			Link<?> link = typeLink.link;
			link.remove(dissolveNetworks);
		});
		list.clear();
	}

	public Set<ConduitType<?>> keySet() {
		return list.stream().map(entry -> entry.type).collect(Collectors.toSet());
	}

	public Collection<Link<?>> values() {
		return list.stream().map(entry -> entry.link).collect(Collectors.toSet());
	}

	public Set<TypeLink<?>> entrySet() {
		return list;
	}
}
