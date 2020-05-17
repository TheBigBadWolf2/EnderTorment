package white_blizz.ender_torment.common.ender_flux;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.utils.ETUtils;
import white_blizz.ender_torment.utils.IEnchantmentList;

public class EnderFluxStorage implements IEnderFluxStorage {
	public static class Builder extends IEnderFluxStorage.Builder<EnderFluxStorage> {
		private Builder() { super(EnderFluxStorage::new); }
	}

	public static Builder New() {
		return new Builder();
	}

	protected int flux;
	protected int capacity;
	protected int maxReceive;
	protected int maxExtract;
	protected double decay, decayed;

	public EnderFluxStorage(int capacity) {
		this(capacity, capacity, capacity, 0, DEFAULT_DECAY);
	}

	public EnderFluxStorage(int capacity, double decay) {
		this(capacity, capacity, capacity, 0, decay);
	}

	public EnderFluxStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer, 0, DEFAULT_DECAY);
	}

	public EnderFluxStorage(int capacity, int maxTransfer, double decay) {
		this(capacity, maxTransfer, maxTransfer, 0, decay);
	}

	public EnderFluxStorage(int capacity, int maxReceive, int maxExtract) {
		this(capacity, maxReceive, maxExtract, 0, DEFAULT_DECAY);
	}

	public EnderFluxStorage(int capacity, int maxReceive, int maxExtract, double decay) {
		this(capacity, maxReceive, maxExtract, 0, decay);
	}

	public EnderFluxStorage(int capacity, int maxReceive, int maxExtract, int flux) {
		this(capacity, maxReceive, maxExtract, flux, DEFAULT_DECAY);
	}

	public EnderFluxStorage(int capacity, int maxReceive, int maxExtract, int flux, double decay) {
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
		this.flux = Math.max(0 , Math.min(capacity, flux));
		this.decay = MathHelper.clamp(decay, 0, 1);
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("flux", flux);
		tag.putDouble("decayed", decayed);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT tag) {
		flux = tag.getInt("flux");
		decayed = tag.getDouble("decayed");
	}

	@Override
	public int receiveEnderFlux(int maxReceive, boolean simulate) {
		if (!canReceiveFlux())
			return 0;
		int fluxReceived = Math.min(capacity - flux, Math.min(this.getMaxReceive(), maxReceive));
		if (!simulate)
			flux += fluxReceived;
		return fluxReceived;
	}

	@Override
	public int extractEnderFlux(int maxExtract, boolean simulate) {
		if (!canExtractFlux())
			return 0;

		int fluxExtracted = Math.min(flux, Math.min(this.getMaxExtract(), maxExtract));
		if (!simulate)
			flux -= fluxExtracted;
		return fluxExtracted;
	}

	@Override
	public int getMaxReceive() {
		return maxReceive;
	}

	@Override
	public int getMaxExtract() {
		return maxExtract;
	}

	@Override
	public int getEnderFluxStored() { return flux; }

	@Override
	public int getMaxEnderFluxStored() {
		return capacity;
	}

	@Override
	public double getDecayRate() {
		if (enchants == null) return decay;
		int decay_resist = enchants.getEnchantments().getOrDefault(ETEnchantments.DECAY_RESIST.get(), 0);
		return decay * (10 - decay_resist) / 10D;
	}

	@Override public boolean canExtractFlux() { return getMaxExtract() > 0; }
	@Override public boolean canReceiveFlux() { return getMaxReceive() > 0; }

	@Override
	public boolean tick() {
		double decay = getDecayRate();
		if (decay > 0 && flux > 0) {
			double dLost = decay * flux;
			Tuple<Integer, Double> split = ETUtils.split(dLost);
			int iLost = split.getA();
			decayed += split.getB();
			if (decayed >= 1) {
				split = ETUtils.split(decayed);
				iLost += split.getA();
				decayed = split.getB();
			}

			flux = Math.max(0, flux - iLost);
			return true;
		}
		return false;
	}

	private IEnchantmentList enchants;

	@Override
	public void setEnchantmentList(IEnchantmentList list) {
		enchants = list;
	}
}
