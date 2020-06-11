package white_blizz.ender_torment.common.tile_entity;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.enchantment.CapabilityEnchantableBlock;
import white_blizz.ender_torment.common.enchantment.ETEnchantments;
import white_blizz.ender_torment.common.enchantment.EnchantableBlock;
import white_blizz.ender_torment.common.enchantment.EnchantableInventoryBlock;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxGenerator;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.utils.*;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxCollectorTE extends ETTileEntity implements ITickableTileEntity {
	private static final Object2IntArrayMap<ResourceLocation> ITEM_2_BURN = new Object2IntArrayMap<>();

	public static void addItemBurn(ResourceLocation item, int burn) {
		ITEM_2_BURN.put(item, burn);
	}

	public static void addItemBurn(IItemProvider item, int burn) {
		ResourceLocation loc = item.asItem().getRegistryName();
		if (loc != null) addItemBurn(loc, burn);
	}

	static {
		addItemBurn(Items.ENDER_PEARL, 100);
		addItemBurn(Items.ENDER_EYE, 500);
	}

	public EnderFluxCollectorTE() {
		super(ETBlocks.ENDER_FLUX_COLLECTOR_TYPE);
		enderFluxStorage.setEnchantmentList(enchantable);
	}

	@Override
	public void tick() {
		if (enderFluxStorage.tickWithResult().isDirty()) markDirty();
	}

	private enum GenTickResult implements IEnderFluxStorage.IEnderFluxTickResult {
		Nothing {@Override GenTickResult decay() { return Decayed; }},
		Burning {@Override GenTickResult decay() { return BurningDecayed; }},
		Consumed {@Override GenTickResult decay() { return ConsumedDecayed; }},
		Decayed {@Override GenTickResult decay() { return this; }},
		BurningDecayed {@Override GenTickResult decay() { return this; }},
		ConsumedDecayed {@Override GenTickResult decay() { return this; }};


		abstract GenTickResult decay();
		boolean isDirty() { return this != Nothing; }
	}

	private interface IAction {
		boolean check();
		default void apply() {}
	}

	private class EnderFluxGen implements IEnderFluxGenerator, IItemHandlerModifiable {
		private int flux;
		private final int capacity, maxExtract;
		private final double decay;
		private double decayed;

		private EnderFluxGen(int capacity, int unused, int maxExtract, int flux, double decay) {
			this.capacity = capacity;
			this.maxExtract = maxExtract;
			this.flux = flux;
			this.decay = decay;

			burnRatio.setMods(
					in -> in,
					out -> (int) (out * getEfficiency() * (1 + (enchants.getLevel(ETEnchantments.ENDER_HEAT) * 0.5))),
					rate -> rate + (enchants.getLevel(ETEnchantments.ENDER_QUANTUM_CHAMBER))
			);
		}

		@Override public int receiveEnderFlux(int maxReceive, boolean simulate) { return 0; }
		@Override
		public int extractEnderFlux(int maxExtract, boolean simulate) {
			if (!canExtractFlux())
				return 0;

			int fluxExtracted = Math.min(flux, Math.min(this.getMaxExtract(), maxExtract));
			if (!simulate)
				flux -= fluxExtracted;
			return fluxExtracted;
		}

		@Override public int getMaxReceive() { return 0; }
		@Override public int getMaxExtract() { return maxExtract; }

		@Override public int getEnderFluxStored() { return flux; }
		@Override public int getMaxEnderFluxStored() { return capacity; }

		@Override public double getDecayRate() {
			int decay_resist = enchants.getLevel(ETEnchantments.DECAY_RESIST);
			return decay * (10 - decay_resist) / 10D;
		}

		@Override public boolean canExtractFlux() { return true; }
		@Override public boolean canReceiveFlux() { return false; }

		private boolean checkSlot() {
			ItemStack stack = getStackInSlot(0);
			if (stack.isEmpty()) return false;
			return isItemValid(0, stack);
		}

		private int cooldown = 0;
		private int burnTime = 0;
		private final Conversion burnRatio = new Conversion(1, 1, 1);

		private GenTickResult tryBurnItemWithAction(IAction action) {
			if (burnTime > 0) {
				Conversion.Ratio ratio = burnRatio.calculate(
						new Conversion.Container(() -> burnTime),
						new Conversion.Container(
								this::getEnderFluxStored,
								this::getMaxEnderFluxStored
						)
				);
				burnTime -= ratio.getIn();
				flux += ratio.getOut();
				return GenTickResult.Burning;
			} else {
				ItemStack stack = getStackInSlot(0);
				if (!stack.isEmpty()) {
					int power = ITEM_2_BURN.getOrDefault(stack.getItem().getRegistryName(), 0);
					if (power > 0) {
						power *= getEfficiency();
						if (action.check()) {
							burnTime = power;
							action.apply();
							stack.shrink(1);
							return GenTickResult.Consumed;
						}
					} else if (world != null) {
						Vec3d pos = new Vec3d(getPos()).add(0.5, 1.5, 0.5);
						FireworkRocketEntity rocket = FireworkMaker.New()
								.setFlight((byte) 1)
								.newStar()
								.setTrail().done()
								.spawn(world, pos)
								;
						ItemEntity item = new ItemEntity(
								world,
								pos.getX(), pos.getY(), pos.getZ(),
								fuel
						);
						fuel = ItemStack.EMPTY;
						world.addEntity(item);
						item.startRiding(rocket, true);
						return GenTickResult.Consumed;
					}
				}
			}
			return GenTickResult.Nothing;
		}

		private GenTickResult tryBurnItem() {
			return tryBurnItemWithAction(() -> true);
		}

		private GenTickResult tryBurnItemWithEject() {
			return tryBurnItemWithAction(new IAction() {
				LivingEntity target;

				@Override
				public boolean check() {
					if (world != null) {
						world.getEntitiesWithinAABB(LivingEntity.class,
								new AxisAlignedBB(getPos()).grow(getRange()))
								.stream().filter(EnderFluxCollectorTE.this::inRange)
								.findAny()
								.ifPresent(entity -> target = entity);
						return target != null;
					}
					return false;
				}

				@Override
				public void apply() {
					assert world != null;
					EnderPearlEntity enderPearl = new EnderPearlEntity(world, target);
					Vec3d pos = new Vec3d(getPos()).add(.5, 1.5, .5);
					enderPearl.setPositionAndUpdate(pos.x, pos.y, pos.z);
					Vec3d motion = ETMath.nextVec(world.rand, 1, 0, 1);
					enderPearl.setMotion(motion);
					world.addEntity(enderPearl);
				}
			});
		}

		@Override
		public GenTickResult tickWithResult() {
			GenTickResult result = tryBurnItem();

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
				return result.decay();
			}
			return result;
		}

		private IEnchantmentList enchants;

		@Override
		public void setEnchantmentList(IEnchantmentList list) {
			enchants = list;
		}

		//protected NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
		private ItemStack fuel = ItemStack.EMPTY;

		@Override
		public CompoundNBT serializeNBT() {
			ListNBT stacks = new ListNBT();

			if (!fuel.isEmpty()){
				CompoundNBT itemTag = new CompoundNBT();
				itemTag.putInt("Slot", 0);
				fuel.write(itemTag);
				stacks.add(itemTag);
			}

			CompoundNBT tag = new CompoundNBT();
			tag.put("Items", stacks);

			tag.putInt("flux", flux);
			tag.putDouble("decayed", decayed);
			tag.putInt("cooldown", cooldown);
			tag.putInt("burnTime", burnTime);
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundNBT tag) {
			ListNBT tagList = tag.getList("Items", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.size(); i++) {
				CompoundNBT itemTags = tagList.getCompound(i);
				int slot = itemTags.getInt("Slot");

				if (slot == 0) fuel = ItemStack.read(itemTags);
			}
			flux = tag.getInt("flux");
			decayed = tag.getDouble("decayed");
			cooldown = tag.getInt("cooldown");
			burnTime = tag.getInt("burnTime");
		}

		private void charge(int amount) {
			flux = Math.min(flux + amount, capacity);
		}

		@Override
		public int getSlots() {
			return 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot == 0) return fuel;
			return ItemStack.EMPTY;
		}

		protected void validateSlotIndex(int slot) {
			if (slot < 0 || slot >= getSlots())
				throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
		}

		protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
			return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
		}

		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (slot == 0) {
				if (!isItemValid(0, stack)) return stack;

				int limit = getStackLimit(slot, stack);

				if (!fuel.isEmpty()) {
					if (!ItemHandlerHelper.canItemStacksStack(stack, fuel))
						return stack;

					limit -= fuel.getCount();
				}

				if (limit <= 0) return stack;

				boolean reachedLimit = stack.getCount() > limit;

				if (!simulate) {
					if (fuel.isEmpty()) {
						fuel = (reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
					} else {
						fuel.grow(reachedLimit ? limit : stack.getCount());
					}
				}

				return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
			}
			return stack;
		}

		@Override
		@Nonnull
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (amount == 0) return ItemStack.EMPTY;

			if (slot == 0) {
				return ItemStack.EMPTY;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot == 0) return 64;
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			if (slot == 0)
				return ITEM_2_BURN.containsKey(stack.getItem().getRegistryName());
			return false;
		}

		@Override public int getBurnTime() { return burnTime; }
		@Override public Conversion getConverter() { return burnRatio; }

		@Override
		public int getEfficiency() {
			return enchants.getLevel(ETEnchantments.ENDER_DISPERSER) + 1;
		}

		@Override
		public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
			if (slot == 0) fuel = stack;
		}
	}

	private final EnderFluxGen enderFluxStorage =
			IEnderFluxStorage.New(EnderFluxGen::new).outputOnly(1000).build();

	private final EnchantableInventoryBlock enchantable = new EnchantableInventoryBlock(new ListNBT());



	public boolean inRange(Vec3d pos) {
		return getPos().withinDistance(pos, getRange());
	}

	public boolean inRange(Entity entity) {
		return getPos().withinDistance(entity.getPositionVec(), getRange());
	}

	public double getRange() {
		return 16D;
	}

	public void charge(int amount) {
		enderFluxStorage.charge(amount);
		markDirty();
	}

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
		return CapList.New().addCapS(CapabilityEnderFlux.ENDER_FLUX, () -> enderFluxStorage)
				.addCapS(CapabilityEnchantableBlock.ENCHANTABLE_BLOCK, () -> enchantable)
				.addCapS(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> new CombinedInvWrapper(enderFluxStorage, enchantable))//ToDo: Make a wrapper that can recalculate.
				.build();
	}
}
