package white_blizz.ender_torment.utils;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ISidedConfig extends IConfig {
	public abstract Dist getSide();

	@Override
	protected final ModConfig.Type getType() {
		final Dist side = getSide();
		if (side.isClient()) return ModConfig.Type.CLIENT;
		else if (side.isDedicatedServer()) return ModConfig.Type.SERVER;
		else throw new IllegalStateException(String.format("Dist %s was neither client nor server!", side));
	}
}
