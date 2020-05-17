package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import white_blizz.ender_torment.utils.FireworkMaker;

import javax.annotation.ParametersAreNonnullByDefault;

//ToDo: Modifiable via enchantments
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderGunItem extends ETItem {

	public EnderGunItem() {
		super(new Properties().maxStackSize(1).group(ItemGroup.COMBAT));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand handIn) {
		if (!worldIn.isRemote) {
			FireworkMaker.New()
					.newStar()
					.setTrail()
					.setType(FireworkRocketItem.Shape.LARGE_BALL)
					.addColor(0xFF0000)
					.addColor(0xFFFF00)
					.addColor(0x00FF00)
					.addColor(0x00FFFF)
					.addColor(0x0000FF)
					.addColor(0xFF00FF)
					.done()
					.spawn(worldIn, player.getEyePosition(1F), true,
							() -> firework -> {
								Vec3d vec3d1 = player.getUpVector(1.0F);
								Quaternion quaternion = new Quaternion(new Vector3f(vec3d1), 0F, true);
								Vec3d vec3d = player.getLook(1.0F);
								Vector3f vector3f = new Vector3f(vec3d);
								vector3f.transform(quaternion);
								firework.shoot(vector3f.getX(), vector3f.getY(), vector3f.getZ(), 1, 0);
							})
			;
		}
		return ActionResult.resultSuccess(player.getHeldItem(handIn));
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {

	}

	@Override
	public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {

	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		return super.onItemUseFinish(stack, worldIn, entityLiving);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return super.getUseDuration(stack);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return super.getUseAction(stack);
	}
}
