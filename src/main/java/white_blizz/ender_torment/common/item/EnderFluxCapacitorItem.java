package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import white_blizz.ender_torment.common.enchantment.EnchantableInventoryBlock;
import white_blizz.ender_torment.common.ender_flux.EnderFluxStorage;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.utils.InfoUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxCapacitorItem extends ETItem {
	@CapabilityInject(IEnderFluxStorage.class)
	public static Capability<IEnderFluxStorage> ENDER_FLUX_CAP = null;

	public EnderFluxCapacitorItem() {
		super(new Properties().maxStackSize(1).group(ETItems.ENDER_MISC));
	}

	private static class CapProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
		private final EnderFluxStorage storage = new EnderFluxStorage(1000, 0D);
		private final ItemStack stack;

		private CapProvider(ItemStack stack) { this.stack = stack; }

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			if (cap == ENDER_FLUX_CAP) return ENDER_FLUX_CAP.orEmpty(cap, LazyOptional.of(() -> storage));
			return LazyOptional.empty();
		}


		@Override
		public CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.put("flux", storage.serializeNBT());
			return tag;
		}

		@Override
		public void deserializeNBT(CompoundNBT tag) {
			storage.deserializeNBT(tag.getCompound("flux"));
		}
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new CapProvider(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(
			ItemStack stack, @Nullable World worldIn,
			List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (ENDER_FLUX_CAP == null) return; //Because JEI probes items before the Caps are injected.
		stack.getCapability(ENDER_FLUX_CAP).ifPresent(storage -> InfoUtils.parseInfo2(storage, txtStyle -> {
			switch (txtStyle) {
				case INFO: return TextFormatting.WHITE;
				case WARNING: return TextFormatting.YELLOW;
				case ERROR: return TextFormatting.DARK_RED;
				case POSITIVE: return TextFormatting.GREEN;
				case NEUTRAL: return TextFormatting.BLUE;
				case NEGATIVE: return TextFormatting.RED;
				case RESET: return TextFormatting.RESET;
				default: return null;
			}
		}).forEach(line -> tooltip.add(new StringTextComponent(line))));
	}
}
