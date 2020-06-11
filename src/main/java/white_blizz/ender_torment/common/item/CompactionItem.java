package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import white_blizz.ender_torment.common.block.CompactionBlock;
import white_blizz.ender_torment.common.tile_entity.CompactionTE;
import white_blizz.ender_torment.utils.ETNBTUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompactionItem extends ETItem {
	private final CompactionBlock block;

	public CompactionItem(Supplier<? extends CompactionBlock> block, Properties properties) {
		super(properties);
		this.block = block.get();
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		UUID id = ETNBTUtil.UUID.getAs(stack.getOrCreateTag(), "id", UUID::randomUUID);
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Direction dir = context.getFace().getOpposite();

		if (!context.func_225518_g_()) {
			Vec3i min, max;
			int radius = 1;
			min = pos.add(-radius, -radius, -radius).offset(dir, radius);
			max = pos.add(radius, radius, radius).offset(dir, radius);

			List<BlockPos> poses = new ArrayList<>();
			BlockPos.Mutable mPos = new BlockPos.Mutable();
			for (mPos.setX(min.getX()); mPos.getX() <= max.getX(); mPos.move(1, 0, 0)) {
				for (mPos.setY(min.getY()); mPos.getY() <= max.getY(); mPos.move(0, 1, 0)) {
					for (mPos.setZ(min.getZ()); mPos.getZ() <= max.getZ(); mPos.move(0, 0, 1)) {
						poses.add(mPos.toImmutable());
					}
				}
			}

			CompactionTE.collect(world, pos, context.getPlacementHorizontalFacing(), id, poses);
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		if (player.isSecondaryUseActive()) {
			ItemStack stack = player.getHeldItem(hand);
			UUID id = ETNBTUtil.UUID.getAs(stack.getOrCreateTag(), "id", UUID::randomUUID);
			Direction dir = player.getHorizontalFacing();
			CompactionTE.release(world, player.getPosition().up().offset(dir), dir, id);
			return ActionResult.resultSuccess(stack);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		block.addInformation(stack, worldIn, tooltip, flagIn);
	}
}
