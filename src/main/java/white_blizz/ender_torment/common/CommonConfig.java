package white_blizz.ender_torment.common;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.utils.IConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommonConfig extends IConfig {
	public static CommonConfig get() { return (CommonConfig) EnderTorment.getINSTANCE().COMMON_CONFIG; }
	@Override protected ModConfig.Type getType() { return ModConfig.Type.COMMON; }

	public final ForgeConfigSpec.BooleanValue decay_enabled;
	public final ForgeConfigSpec.ConfigValue<Double> default_decay_rate;

	public CommonConfig() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.push("ender_flux.decay");
		decay_enabled = builder.define("decay_enabled", false);
		default_decay_rate = builder.define("default_decay_rate", 0.00005);
		builder.pop(2);

		register(builder);
	}
}
