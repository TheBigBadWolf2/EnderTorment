package white_blizz.ender_torment.common.ender_flux;

import net.minecraft.util.DamageSource;

public class EnderFluxDamageSource extends DamageSource {

	public EnderFluxDamageSource() {
		super("damageTypeIn");
		setDamageBypassesArmor();
		setDamageIsAbsolute();
	}
}
