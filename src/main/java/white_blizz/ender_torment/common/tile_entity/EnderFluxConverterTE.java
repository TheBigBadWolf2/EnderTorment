package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.enchantment.CapabilityEnchantableBlock;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.common.enchantment.EnchantableBlock;
import white_blizz.ender_torment.common.enchantment.EnchantableInventoryBlock;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.ender_flux.EnderFluxStorage;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.utils.Conversion;
import white_blizz.ender_torment.utils.ETUtils;
import white_blizz.ender_torment.utils.IEnchantmentList;
import white_blizz.ender_torment.utils.TransUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxConverterTE extends ETTileEntity implements ITickableTileEntity {
	public EnderFluxConverterTE() {
		super(ETBlocks.ENDER_FLUX_CONVERTER_TYPE);
		enderFluxStorage.setEnchantmentList(enchantable);
	}

	@Override
	public void tick() {
		/*for (Direction direction : Direction.values()) {
			ETUtils.mapToLazy(ETUtils.getTileEntity(getWorld(), getPos().offset(direction)),
					te -> te.getCapability(CapabilityEnderFlux.ENDER_FLUX)).ifPresent(storage ->
					TransUtils.transfer(storage, enderFluxStorage));
		}*/
		enderFluxStorage.tick();
		for (Direction direction : Direction.values()) {
			ETUtils.mapToLazy(ETUtils.getTileEntity(getWorld(), getPos().offset(direction)),
					te -> te.getCapability(CapabilityEnergy.ENERGY)).ifPresent(storage ->
					TransUtils.transfer(enderFluxStorage, storage));
		}
	}

	private static class EnderFluxConverter implements IEnderFluxStorage, IEnergyStorage {

		private final Conversion convert = new Conversion(10, 1, 1);

		private int flux, energy;
		private final int capacity, maxReceive, maxExtract;
		private final double decay;
		private double decayed;

		private EnderFluxConverter(int capacity, int maxReceive, int maxExtract, int flux, double decay) {
			this.capacity = capacity;
			this.maxReceive = maxReceive;
			this.maxExtract = maxExtract;
			this.flux = flux;
			this.decay = decay;
		}

		@Override
		public int receiveEnderFlux(int maxReceive, boolean simulate) {
			if (!canReceiveFlux()) return 0;
			int fluxReceived = Math.min(capacity - flux, Math.min(this.getMaxReceive(), maxReceive));
			if (!simulate)
				flux += fluxReceived;
			return fluxReceived;
		}

		@Override public int extractEnderFlux(int maxExtract, boolean simulate) { return 0; }

		@Override public int getMaxReceive() { return maxReceive; }
		@Override public int getMaxExtract() { return 0; }

		@Override public int getEnderFluxStored() { return flux; }

		@Override public int getMaxEnderFluxStored() { return capacity; }

		@Override public double getDecayRate() {
			int decay_resist = enchants.getEnchantments().getOrDefault(ETEnchantments.DECAY_RESIST.get(), 0);
			return decay * (10 - decay_resist) / 10D;
		}

		@Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!canExtract()) return 0;

			int energyExtracted = Math.min(flux, Math.min(this.maxExtract, maxExtract));
			if (!simulate)
				flux -= energyExtracted;
			return energyExtracted;
		}

		@Override public int getEnergyStored() { return energy; }
		@Override public int getMaxEnergyStored() { return capacity; }

		@Override public boolean canExtract() { return true; }
		@Override public boolean canReceive() { return false; }

		@Override public boolean canExtractFlux() { return false; }
		@Override public boolean canReceiveFlux() { return true; }

		@Override
		public boolean tick() {
			boolean flag = false;
			if (flux > 0 && energy < capacity) {
				Conversion.Ratio min = convert.min(Math.min(flux, convert.inputRate()), Math.min(capacity - energy, convert.outputRate()));

				if (!min.isZero()) {
					flux -= min.getIn();
					energy += min.getOut();
					flag = true;
				}
			}

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
				flag = true;
			}
			return flag;
		}

		private IEnchantmentList enchants;

		@Override
		public void setEnchantmentList(IEnchantmentList list) {
			enchants = list;
		}

		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("energy", energy);
			tag.putInt("flux", flux);
			tag.putDouble("decayed", decayed);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundNBT tag) {
			energy = tag.getInt("energy");
			flux = tag.getInt("flux");
			decayed = tag.getDouble("decayed");
		}
	}

	private final EnderFluxConverter enderFluxStorage =
			IEnderFluxStorage.New(EnderFluxConverter::new).io(1000).build();

	private final EnchantableInventoryBlock enchantable = new EnchantableInventoryBlock(new ListNBT());

	@Override
	protected void extraRead(CompoundNBT compound) {
		enderFluxStorage.deserializeNBT(compound.getCompound("flux"));
		enchantable.deserializeNBT(compound.getList("Enchantments", 10));
	}

	@Override
	protected void extraWrite(CompoundNBT compound) {
		compound.put("flux", enderFluxStorage.serializeNBT());
		compound.put("Enchantments", enchantable.serializeNBT());
	}

	@Override
	protected List<Cap<?>> getCaps() {
		return ImmutableList.of(
				new Cap<>(CapabilityEnderFlux.ENDER_FLUX, () -> enderFluxStorage),
				new Cap<>(CapabilityEnergy.ENERGY, () -> enderFluxStorage),
				new Cap<>(CapabilityEnchantableBlock.ENCHANTABLE_BLOCK, () -> enchantable),
				new Cap<>(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> enchantable)
		);
	}
}
