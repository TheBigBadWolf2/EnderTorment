package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import white_blizz.ender_torment.common.ETRegistry;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.conduit.ConduitType;
import white_blizz.ender_torment.common.tile_entity.ConduitTE;
import white_blizz.ender_torment.common.tile_entity.ETTileEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BaseConduitItem extends ETItem {
	public BaseConduitItem() {
		super(new Item.Properties().group(ETItems.ENDER_MISC));
	}

	private static class Response {
		private final BlockPos pos;
		private final boolean adding;
		private final boolean tryAgain;

		private Response(BlockPos pos, boolean adding) {
			this(pos, adding, false);
		}
		private Response(BlockPos pos, boolean adding, boolean tryAgain) {
			this.pos = pos;
			this.adding = adding;
			this.tryAgain = tryAgain;
		}
	}

	private Optional<Response> getFirstPos(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == ETBlocks.CONDUIT.get()) return Optional.of(new Response(pos, true, true));
		BlockItemUseContext blockContext = new BlockItemUseContext(context);
		if (state.isReplaceable(blockContext)) return Optional.of(new Response(pos, false, true));
		return getNextPos(context);
	}

	private Optional<Response> getNextPos(ItemUseContext context) {
		BlockItemUseContext blockContext = new BlockItemUseContext(context);
		World world = context.getWorld();
		BlockPos pos = context.getPos().offset(context.getFace());
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == ETBlocks.CONDUIT.get()) return Optional.of(new Response(pos, true));
		if (state.isReplaceable(blockContext)) return Optional.of(new Response(pos, false));
		return Optional.empty();
	}

	private boolean tryAt(World world, BlockPos pos, boolean adding, ConduitType<?> type) {
		if (adding || world.setBlockState(pos, ETBlocks.CONDUIT.get().getDefaultState()))
			return ETTileEntity.get(ConduitTE.class, world, pos)
					.map(conduit -> conduit.addType(type)).orElse(false);
		return false;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			ETRegistry.CONDUIT_TYPE.getValues().forEach(type -> items.add(fromType(type)));
		}
	}

	@Nullable
	public ConduitType<?> getType(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		if (tag != null && tag.contains("conduit", Constants.NBT.TAG_STRING)) {
			String name = tag.getString("conduit");
			return ETRegistry.CONDUIT_TYPE.getValue(new ResourceLocation(name));
		}

		return null;
	}

	public ItemStack fromType(ConduitType<?> type) {
		ItemStack stack = new ItemStack(this);
		stack.setTagInfo("conduit", StringNBT.valueOf(Objects.requireNonNull(type.getRegistryName()).toString()));
		return stack;
	}

	@Override @OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ConduitType<?> type = getType(stack);
		if (type != null) {
			tooltip.add(new TranslationTextComponent(type.getTranslationKey()));
		}
	}

	public String getTranslationKey(ItemStack stack) {
		ConduitType<?> type = getType(stack);
		if (type != null) return this.getTranslationKey() + "." + type.getRegistryName();
		return this.getTranslationKey();
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		ConduitType<?> type = getType(context.getItem());
		if (type == null) return ActionResultType.FAIL;

		World world = context.getWorld();
		if (world.isRemote()) return ActionResultType.PASS;
		/*BlockPos pos = context.getPos();
		boolean adding = false;
		{
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() == ETBlocks.CONDUIT.get()) adding = true;
			else if (!state.isReplaceable(new BlockItemUseContext(context))) {
				pos = pos.offset(context.getFace());
				if (!world.getBlockState(pos).isReplaceable(new BlockItemUseContext(context)))
					return ActionResultType.FAIL;
			}
		}

		if (adding || world.setBlockState(pos, ETBlocks.CONDUIT.get().getDefaultState()))
			return ETTileEntity.get(ConduitTE.class, world, pos)
					.filter(conduit -> conduit.addType(type))
					.map(conduit -> {
						context.getItem().shrink(1);
						return ActionResultType.CONSUME;
					})
					.orElse(ActionResultType.FAIL)
					;

		return ActionResultType.FAIL;*/
		Optional<Response> optRes = getFirstPos(context);
		if (optRes.isPresent()) {
			Response response = optRes.get();
			if (tryAt(world, response.pos, response.adding, type)) {
				context.getItem().shrink(1);
				return ActionResultType.CONSUME;
			} else if (response.tryAgain) {
				optRes = getNextPos(context);
				if (optRes.isPresent()) {
					response = optRes.get();
					if (tryAt(world, response.pos, response.adding, type)) {
						context.getItem().shrink(1);
						return ActionResultType.CONSUME;
					}
				}
			}
		}
		return ActionResultType.FAIL;
	}
}
