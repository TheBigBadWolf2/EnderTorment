package white_blizz.ender_torment.common.enchantment;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class EnchantableInventoryBlock extends EnchantableBlock implements IItemHandlerModifiable {
	public EnchantableInventoryBlock(ListNBT enchants) {
		super(enchants);
	}

	@Override public int getSlots() { return count() + 1; }

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot < count())
			return EnchantedBookItem.getEnchantedItemStack(getEnchant(slot));
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (slot < count()) return stack;
		if (!isItemValid(slot, stack)) return stack;

		ListNBT enchantments = EnchantedBookItem.getEnchantments(stack).copy();
		if (enchantments.isEmpty()) return stack;
		CompoundNBT tag = (CompoundNBT) enchantments.remove(0);
		if (!simulate) add(tag);

		if (enchantments.isEmpty()) return ItemStack.EMPTY;

		ItemStack copy = stack.copy();
		copy.setTagInfo("StoredEnchantments", enchantments);
		return copy;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot >= count() || amount <= 0) return ItemStack.EMPTY;
		EnchantmentData enchant = getEnchant(slot);

		if (!simulate) remove(enchant.enchantment);

		return EnchantedBookItem.getEnchantedItemStack(enchant);
	}

	@Override public int getSlotLimit(int slot) { return 1; }

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return !hasEnchants(stack);
	}

	private boolean hasEnchants(ItemStack stack) {
		return EnchantedBookItem.getEnchantments(stack).isEmpty();
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (hasEnchants(stack)) {
			ListNBT enchants = EnchantedBookItem.getEnchantments(stack);
			if (enchants.size() == 1) {
				this.enchants.set(slot, enchants.get(0));
				return;
			}
		}
		throw new IllegalStateException("Item \""+stack.getItem().getRegistryName()+"\" was invalid for EnchantableInventoryBlock!");
	}
}
