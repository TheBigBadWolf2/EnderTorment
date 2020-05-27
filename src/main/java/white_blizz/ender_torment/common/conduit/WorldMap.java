package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;

public class WorldMap extends HashMap<BlockPos, BlockMap> {
	@Nullable
	<Cap> Link<Cap> get(BlockPos pos, ConduitType<Cap> type) {
		final BlockMap map = get(pos);
		if (map == null) return null;
		return map.get(type);
	}
	<Cap> void add(Link<Cap> link) { computeIfAbsent(link.pos, p -> new BlockMap()).add(link); }
	<Cap> void remove(Link<Cap> link) {
		final BlockMap map = get(link.pos);
		if (map != null) {
			map.remove(link);
			if (map.isEmpty())
				remove(link.pos);
		}
	}
}
