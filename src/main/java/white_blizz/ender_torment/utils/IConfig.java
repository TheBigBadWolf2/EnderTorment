package white_blizz.ender_torment.utils;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class IConfig {
	protected abstract ModConfig.Type getType();

	protected void register(ForgeConfigSpec.Builder builder) {
		ModLoadingContext.get().registerConfig(
				getType(),
				builder.build()
		);
	}


}