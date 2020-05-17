package white_blizz.ender_torment.utils;

import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.util.text.TextFormatting;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxGenerator;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class InfoUtils {
	public enum TxtStyle {
		INFO, WARNING, ERROR,
		POSITIVE, NEUTRAL, NEGATIVE,

		RESET
	}

	public static List<String> parseInfo2(
			IEnderFluxStorage fluxStorage,
			Function<TxtStyle, Object> map
	) {
		return parseInfo(fluxStorage, style -> String.valueOf(map.apply(style)));
	}

	public static List<String> parseInfo(
			IEnderFluxStorage fluxStorage,
			Function<TxtStyle, String> map
	) {
		List<String> list = new ArrayList<>();
		list.add(String.format(
				"%1$sFlux: %2$s%4$d%1$s / %2$s%5$d%3$s",
				map.apply(TxtStyle.INFO),
				map.apply(TxtStyle.POSITIVE),
				map.apply(TxtStyle.RESET),
				fluxStorage.getEnderFluxStored(),
				fluxStorage.getMaxEnderFluxStored()
		));
		if (fluxStorage.canReceiveFlux())
			list.add(String.format(
					"%sMax Input: %s%d%s",
					map.apply(TxtStyle.INFO),//TextStyleClass.INFO,
					map.apply(TxtStyle.POSITIVE),//TextStyleClass.OK,
					fluxStorage.getMaxReceive(),
					map.apply(TxtStyle.RESET)
			));
		if (fluxStorage.canExtractFlux())
			list.add(String.format(
					"%sMax Output: %s%d%s",
					map.apply(TxtStyle.INFO),//TextStyleClass.INFO,
					map.apply(TxtStyle.NEGATIVE),//TextStyleClass.OK,
					fluxStorage.getMaxExtract(),
					map.apply(TxtStyle.RESET)
			));

		if (fluxStorage.getDecayRate() > 0)
			list.add(String.format(
					"%sDecay: %s%f%%%s",
					map.apply(TxtStyle.WARNING),//TextStyleClass.WARNING,
					map.apply(TxtStyle.NEGATIVE),//TextStyleClass.ERROR,
					fluxStorage.getDecayRate() * 100,
					map.apply(TxtStyle.RESET)
			));

		if (fluxStorage instanceof IEnderFluxGenerator) {
			IEnderFluxGenerator generator = (IEnderFluxGenerator) fluxStorage;
			Conversion converter = generator.getConverter();
			list.add(String.format(
					"%sBurn Time: %s%d%s",
					map.apply(TxtStyle.INFO),
					map.apply(TxtStyle.NEUTRAL),
					generator.getBurnTime(),
					map.apply(TxtStyle.RESET)
			));
			list.add(String.format(
					"%4$s%1$d%7$s -> %5$s%2$d%7$s @ %6$s%3$d",
					converter.getModdedRatioIn(),
					converter.getModdedRatioOut(),
					converter.getModdedRate(),

					map.apply(TxtStyle.NEGATIVE),
					map.apply(TxtStyle.POSITIVE),
					map.apply(TxtStyle.NEUTRAL),
					map.apply(TxtStyle.RESET)
			));
		}

		return list;
	}
}
