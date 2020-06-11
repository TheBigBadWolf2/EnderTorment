package white_blizz.ender_torment.intergration.top;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.ILayoutStyle;

public interface IReadOnlyLayoutStyle extends ILayoutStyle {
	@Override default ILayoutStyle borderColor(Integer var1) { return this; }
	@Override default ILayoutStyle spacing(int var1) { return this; }
	@Override default ILayoutStyle alignment(ElementAlignment elementAlignment) { return this; }
}
