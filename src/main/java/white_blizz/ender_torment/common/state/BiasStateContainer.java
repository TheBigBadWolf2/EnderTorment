package white_blizz.ender_torment.common.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.util.MapPopulator;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BiasStateContainer<O, S extends IStateHolder<S>> extends StateContainer<O, S> {
	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	private final O owner;
	private final ImmutableSortedMap<String, IProperty<?>> properties;
	private final ImmutableList<S> validStates;

	@SuppressWarnings("unchecked")
	protected <A extends StateHolder<O, S>> BiasStateContainer(
			O object,
			StateContainer.IFactory<O, S, A> factory,
			Map<String, IProperty<?>> propertiesIn
	) {
		super(object, factory, Collections.emptyMap());
		this.owner = object;
		this.properties = ImmutableSortedMap.copyOf(propertiesIn);
		Map<Map<IProperty<?>, Comparable<?>>, A> map = Maps.newLinkedHashMap();
		List<A> list = Lists.newArrayList();
		Stream<List<Comparable<?>>> stream = Stream.of(Collections.emptyList());

		for(IProperty<?> iproperty : this.properties.values()) {
			stream = stream.flatMap((comparables) -> {
				return iproperty.getAllowedValues().stream().map((o) -> {
					List<Comparable<?>> list1 = Lists.newArrayList(comparables);
					list1.add(o);
					return list1;
				});
			});
		}

		stream.forEach((comparables) -> {
			Map<IProperty<?>, Comparable<?>> map1 = MapPopulator.createMap(this.properties.values(), comparables);
			A a1 = factory.create(object, ImmutableMap.copyOf(map1));
			map.put(map1, a1);
			list.add(a1);
		});

		for(A a : list) {
			a.buildPropertyValueTable((Map<Map<IProperty<?>, Comparable<?>>, S>) map);
		}

		this.validStates = (ImmutableList<S>) ImmutableList.copyOf(list);
	}

	public ImmutableList<S> getValidStates() {
		return this.validStates;
	}

	public S getBaseState() {
		return (S)(this.validStates.get(0));
	}

	public O getOwner() {
		return this.owner;
	}

	public Collection<IProperty<?>> getProperties() {
		return this.properties.values();
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.properties.values().stream().map(IProperty::getName).collect(Collectors.toList())).toString();
	}

	@Nullable
	public IProperty<?> getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
}
