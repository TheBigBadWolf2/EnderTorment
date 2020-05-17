package white_blizz.ender_torment.common.potion;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class ETEffect extends Effect implements IETEffect {
	protected ETEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}
}
