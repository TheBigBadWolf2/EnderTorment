package white_blizz.ender_torment.common.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import white_blizz.ender_torment.client.ClientHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FluxVisionEffect extends ETEffect {
	public FluxVisionEffect() {
		super(EffectType.NEUTRAL, 0x00FF88);
	}

	@Override
	public void applyAttributesModifiersToEntity(LivingEntity entity, AbstractAttributeMap map, int amplifier) {
		super.applyAttributesModifiersToEntity(entity, map, amplifier);
	}

	@Override
	public void removeAttributesModifiersFromEntity(LivingEntity entity, AbstractAttributeMap map, int amplifier) {
		super.removeAttributesModifiersFromEntity(entity, map, amplifier);
	}
}
