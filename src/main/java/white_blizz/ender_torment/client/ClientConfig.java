package white_blizz.ender_torment.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.IConfig;

import java.util.function.Predicate;

public class ClientConfig implements IConfig {
	public static ClientConfig get() {
		return (ClientConfig) EnderTorment.getINSTANCE().SIDED_CONFIG;
	}

	public final ForgeConfigSpec.ConfigValue<String> hostile_color;
	public final ForgeConfigSpec.ConfigValue<String> animal_color;

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
		builder.pop();
		ModLoadingContext.get().registerConfig(
				ModConfig.Type.CLIENT,
				builder.build()
		);
	}

	private static int parse(ForgeConfigSpec.ConfigValue<String> config) {
		return Integer.parseInt(config.get(), 16);
	}

	public int getHostileColor() { return parse(hostile_color); }
	public int getAnimalColor() { return parse(animal_color); }
}
