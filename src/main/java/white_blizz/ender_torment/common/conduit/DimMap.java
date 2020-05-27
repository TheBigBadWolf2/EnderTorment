package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.HashMap;

public class DimMap extends HashMap<DimensionType, WorldMap> {
	@Nullable
	<Cap> Link<Cap> get(DimensionType dim, BlockPos pos, ConduitType<Cap> type) {
		final WorldMap map = get(dim);
		if (map == null) return null;
		return map.get(pos, type);
	}
	<Cap>void add(Link<Cap> link) { computeIfAbsent(link.dim, d -> new WorldMap()).add(link); }
	<Cap> void remove(Link<Cap> link) {
		final WorldMap map = get(link.dim);
		if (map != null) {
			map.remove(link);
			if (map.isEmpty())
				remove(link.dim);
		}
	}
}
