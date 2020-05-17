package white_blizz.ender_torment.utils;

import net.minecraftforge.energy.IEnergyStorage;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;

public final class TransUtils {
	public static boolean transfer(
			IEnderFluxStorage from, IEnderFluxStorage to) {
		return transfer(from, to, Math.min(from.getMaxExtract(), to.getMaxReceive()));
	}
	public static boolean transfer(
			IEnderFluxStorage from, IEnderFluxStorage to,
			int maxTrans) {
		if (maxTrans == 0) return false;
		if (from.canExtractFlux() && to.canReceiveFlux()) {
			int trans = Math.min(
					from.extractEnderFlux(maxTrans, true),
					to.receiveEnderFlux(maxTrans, true)
			);
			if (trans > 0) {
				from.extractEnderFlux(trans, false);
				to.receiveEnderFlux(trans, false);
				return true;
			}
		}
		return false;
	}


	public static boolean transfer(
			IEnergyStorage from, IEnergyStorage to) {
		return transfer(from, to, Integer.MAX_VALUE);
	}
	public static boolean transfer(
			IEnergyStorage from, IEnergyStorage to,
			int maxTrans) {
		if (maxTrans == 0) return false;
		if (from.canExtract() && to.canReceive()) {
			int trans = Math.min(
					from.extractEnergy(maxTrans, true),
					to.receiveEnergy(maxTrans, true)
			);
			if (trans > 0) {
				from.extractEnergy(trans, false);
				to.receiveEnergy(trans, false);
				return true;
			}
		}
		return false;
	}
}
