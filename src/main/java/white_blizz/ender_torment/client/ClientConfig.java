package white_blizz.ender_torment.client;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.utils.ISidedConfig;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClientConfig extends ISidedConfig {
	public static ClientConfig get() { return (ClientConfig) EnderTorment.getINSTANCE().SIDED_CONFIG; }

	public final ForgeConfigSpec.ConfigValue<String> hostile_color;
	public final ForgeConfigSpec.ConfigValue<String> animal_color;
	public final ForgeConfigSpec.BooleanValue show_line;


	private static Predicate<Object> strPred(final Predicate<String> pred) {
		return o -> o instanceof String && pred.test((String) o);
	}

	public ClientConfig() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("ender_vision");
		Predicate<Object> validator = strPred(s -> s.matches("[0-9a-fA-F]{6}"));
		builder.comment("Theses are 6 digit hexadecimal numbers.");
		hostile_color = builder.define("hostile_color", "FF0000", validator);
		animal_color = builder.define("animal_color", "00FF00", validator);
		builder.pop().push("Debug");
		show_line = builder.define("show line", false);
		builder.pop();
		register(builder);
	}

	private static int parse(ForgeConfigSpec.ConfigValue<String> config) {
		return Integer.parseInt(config.get(), 16);
	}

	public int getHostileColor() { return parse(hostile_color); }
	public int getAnimalColor() { return parse(animal_color); }
	public boolean shouldShowLines() { return show_line.get(); }

	@Override public Dist getSide() { return Dist.CLIENT; }
}
