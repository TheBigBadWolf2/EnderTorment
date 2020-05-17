package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import white_blizz.ender_torment.utils.ETDeferredRegisterHandler;

import java.util.Collection;

public final class ETEnchantments extends ETDeferredRegisterHandler {
	private static final DeferredRegister<Enchantment> ENCHANTMENTS = New(ForgeRegistries.ENCHANTMENTS);

	public static final RegistryObject<DecayResistEnchantment> DECAY_RESIST = ENCHANTMENTS.register("decay_resist", DecayResistEnchantment::new);

	public static final RegistryObject<EnderGenEnchantment> ENDER_DISPERSER = ENCHANTMENTS.register("ender_disperser", () -> new EnderGenEnchantment(Enchantment.Rarity.RARE){
		@Override public int getMaxLevel() { return 5; }
	});
	public static final RegistryObject<EnderGenEnchantment> ENDER_HEAT = ENCHANTMENTS.register("ender_heat", () -> new EnderGenEnchantment(Enchantment.Rarity.RARE){
		@Override public int getMaxLevel() { return 5; }
	});
	public static final RegistryObject<EnderGenEnchantment> ENDER_QUANTUM_CHAMBER = ENCHANTMENTS.register("ender_quantum_chamber", () -> new EnderGenEnchantment(Enchantment.Rarity.RARE){
		@Override public int getMaxLevel() { return 5; }
	});


	public ETEnchantments(IEventBus bus) { super(bus, ENCHANTMENTS); }

	public static Collection<RegistryObject<Enchantment>> getEnchantments() {
		return ENCHANTMENTS.getEntries();
	}

	@Override public boolean regModBus() { return false; }
	@Override public boolean regForgeBus() { return false; }
}
