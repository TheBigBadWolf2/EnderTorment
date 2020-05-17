package white_blizz.ender_torment.common.potion;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

import java.util.Arrays;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class ETEffects extends ETDeferredRegisterHandler {
	private static final DeferredRegister<Effect> EFFECTS = New(ForgeRegistries.POTIONS);
	private static final PotionReg POTIONS = New(ForgeRegistries.POTION_TYPES, PotionReg::new);

	private static class PotionReg extends ETDeferredRegister<Potion> {
		private PotionReg(IForgeRegistry<Potion> reg, String mod_id) { super(reg, mod_id); }

		@SafeVarargs
		private final RegistryObject<Potion> reg(String name, Supplier<EffectInstance>... effects) {
			return register(name, () -> new Potion(name, Arrays.stream(effects).map(Supplier::get).toArray(EffectInstance[]::new)));
		}
		private RegistryObject<Potion> reg(String name, EffectInstance... effects) {
			return register(name, () -> new Potion(name, effects));
		}
	}

	public static final RegistryObject<FluxVisionEffect> FLUX_VISION = EFFECTS.register("flux_vision", FluxVisionEffect::new);
	/*public static final RegistryObject<Potion> FLUX_VISION_1 = POTIONS.reg("flux_vision", () -> {
		EffectInstance instance = null;
		int timePer = 10 * 20;
		for (int i = 5; i >= 0; i--) {
			instance = new EffectInstance(FLUX_VISION.get(), timePer * (i + 1), i, false, true, true, instance);
		}

		return instance;
	});*/

	public ETEffects(IEventBus bus) { super(bus, EFFECTS, POTIONS); }

	@SubscribeEvent
	public void tooltip(ItemTooltipEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player == null) return;
		EffectInstance effect = player.getActivePotionEffect(FLUX_VISION.get());
		if (effect == null) return;
		event.getToolTip().forEach(txt -> txt.applyTextStyle(TextFormatting.OBFUSCATED));
	}



	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return true; }
}
