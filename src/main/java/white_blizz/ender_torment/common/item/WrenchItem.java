package white_blizz.ender_torment.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class WrenchItem extends ETItem {
	public WrenchItem() {
		super(new Item.Properties().group(ETItems.ENDER_MISC)
				.addToolType(ToolType.get("wrench"), 1));
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
		return world.getBlockState(pos).isToolEffective(ToolType.get("wrench"));
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState oldState = world.getBlockState(pos);
		BlockState newState = oldState.rotate(world, pos, Rotation.CLOCKWISE_90);
		if (oldState != newState && world.setBlockState(pos, newState))
			return ActionResultType.SUCCESS;
		return ActionResultType.PASS;
	}
}
