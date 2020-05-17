package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import white_blizz.ender_torment.common.potion.ETEffects;
import white_blizz.ender_torment.utils.PotionBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluxVisionItem extends ETItem {
	public FluxVisionItem() {
		super(new Properties().group(ETItems.ENDER_MISC).maxStackSize(1));
	}
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 20 * 10;
	}


		@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (playerIn.isPotionActive(Effects.NAUSEA))
			return ActionResult.resultFail(stack);
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(stack);
	}

	@Override
	public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

	}

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
		return oldStack.getItem() == newStack.getItem();
	}

	@Override
	public void onPlayerStoppedUsing(
			ItemStack stack, World worldIn,
			LivingEntity entityLiving, int timeLeft) {
		if (entityLiving.isPotionActive(ETEffects.FLUX_VISION.get())) return;
		EffectInstance effect = new EffectInstance(Effects.NAUSEA, timeLeft);
		effect.setCurativeItems(new ArrayList<>());
		entityLiving.addPotionEffect(effect);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		EffectInstance instance = new PotionBuilder(ETEffects.FLUX_VISION.get(), true, true, true)
				.map(amp -> {
					amp++;
					if (amp == 5) return null;
					return new PotionBuilder.DurAmp(20 * 10, amp);
				}).map(amp -> {
					amp--;
					if (amp < 0) return null;
					return new PotionBuilder.DurAmp(20 * 5, amp);
				}).build();
		entityLiving.addPotionEffect(instance);

		return stack;
	}
}
