package white_blizz.ender_torment.intergration.top;

import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.NumberFormat;

public interface IReadOnlyProgressStyle extends IProgressStyle {
	@Override default IProgressStyle borderColor(int i) { return this; }
	@Override default IProgressStyle backgroundColor(int var1) { return this; }

	@Override default IProgressStyle filledColor(int var1) { return this; }
	@Override default IProgressStyle alternateFilledColor(int var1) { return this; }

	@Override default IProgressStyle showText(boolean var1) { return this; }
	@Override default IProgressStyle numberFormat(NumberFormat var1) { return this; }

	@Override default IProgressStyle prefix(String var1) { return this; }
	@Override default IProgressStyle suffix(String var1) { return this; }

	@Override default IProgressStyle width(int var1) { return this; }
	@Override default IProgressStyle height(int var1) { return this; }

	@Override default IProgressStyle lifeBar(boolean var1) { return this; }
	@Override default IProgressStyle armorBar(boolean var1) { return this; }
}
