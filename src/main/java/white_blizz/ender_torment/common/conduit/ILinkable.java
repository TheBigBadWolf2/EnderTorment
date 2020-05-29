package white_blizz.ender_torment.common.conduit;

import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.Map;

public interface ILinkable {
	boolean addType(ConduitType<?> type);
	boolean removeType(ConduitType<?> type);
	@Nullable<Cap> Link<Cap> getLink(ConduitType<Cap> type);
	Map<ConduitType<?>, Link<?>> getLinks();

	<Cap> boolean setConnection(Direction side, ConduitType<Cap> type, boolean on);
}
