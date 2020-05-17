package white_blizz.ender_torment.common.ender_flux;

import white_blizz.ender_torment.utils.Conversion;

public interface IEnderFluxGenerator extends IEnderFluxStorage {
	int getBurnTime();
	Conversion getConverter();
	int getEfficiency();
}
