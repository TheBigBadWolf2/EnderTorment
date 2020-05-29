package white_blizz.ender_torment.common.conduit;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import white_blizz.ender_torment.common.conduit.io.IConduitBuffer;
import white_blizz.ender_torment.common.conduit.io.IConduitInput;
import white_blizz.ender_torment.common.conduit.io.IConduitOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static white_blizz.ender_torment.common.conduit.ConduitTypes.RNG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemConduitTypeHandler implements TransWrapHandler<IItemHandler> {
	@Override
	public void handle(List<IConduitInput<IItemHandler>> ins, List<IConduitBuffer<IItemHandler>> buffs, List<IConduitOutput<IItemHandler>> outs) {
		if (ins.isEmpty() || outs.isEmpty()) return;
		IItemHandler input = ins.get(RNG.nextInt(ins.size())).getCap();
		IItemHandler output = outs.get(RNG.nextInt(outs.size())).getCap();

		int in = -1;
		int out = -1;
		ItemStack stack1 = null;
		ItemStack stack2 = null;

		for (int i = 0; i < input.getSlots(); i++) {
			stack1 = input.extractItem(i, 1, true);
			if (stack1.isEmpty()) continue;
			in = i;
			break;
		}
		if (stack1 == null || stack1.isEmpty() || in < 0) return;

		for (int i = 0; i < output.getSlots(); i++) {
			stack2 = output.insertItem(i, stack1, true);
			if (!stack2.isEmpty()) continue;
			out = i;
			break;
		}
		if (stack2 == null || !stack2.isEmpty() || out < 0) return;

		output.insertItem(out, input.extractItem(in, 1, false), false);
	}

	@Override
	public LazyOptional<? extends IItemHandler> getCap(TileEntity te, @Nullable Direction dir) {
		if (te instanceof IInventory) {
			if (te instanceof ISidedInventory) return SidedInvWrapper.create((ISidedInventory) te, dir)[0];
			else LazyOptional.of(() -> new InvWrapper((IInventory) te));
		}
		return LazyOptional.empty();
	}
}
