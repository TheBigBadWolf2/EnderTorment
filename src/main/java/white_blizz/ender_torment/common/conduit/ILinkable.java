package white_blizz.ender_torment.common.conduit;

import javax.annotation.Nullable;
import java.util.Map;

public interface ILinkable {
	boolean addType(ConduitType<?> type);
	boolean removeType(ConduitType<?> type);
	@Nullable<Cap> Link<Cap> getLink(ConduitType<Cap> type);
	Map<ConduitType<?>, Link<?>> getLinks();
}
