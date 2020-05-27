package white_blizz.ender_torment.server;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.utils.ISidedConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ServerConfig extends ISidedConfig {
	public static ServerConfig get() { return (ServerConfig) EnderTorment.getINSTANCE().SIDED_CONFIG; }

	@Override public Dist getSide() { return Dist.DEDICATED_SERVER; }
}
