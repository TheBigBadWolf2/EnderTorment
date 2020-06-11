package white_blizz.ender_torment.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import white_blizz.ender_torment.common.dim.ETDims;
import white_blizz.ender_torment.utils.TeleTools;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DebugItem extends ETItem {
	public DebugItem() { super(new Properties().group(ETItems.ENDER_MISC)); }

	private static final ITeleporter TELEPORTER = new ITeleporter() {
		@Nullable @Override
		public Entity placeEntity(Entity entity, ServerWorld srcWorld, ServerWorld dstWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
			if (entity instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) entity;
				double x = entity.getPosX();
				double y = entity.getPosY();
				double z = entity.getPosZ();
				float pitch = entity.rotationPitch;
				float yaw1 = entity.rotationYaw;
				srcWorld.getProfiler().startSection("moving");
				double moveFactor = srcWorld.getDimension().getMovementFactor() / dstWorld.getDimension().getMovementFactor();
				x *= moveFactor;
				z *= moveFactor;

				//Chance to update pos here

				entity.setLocationAndAngles(x, y, z, yaw1, pitch);
				srcWorld.getProfiler().endStartSection("placing");
				double minX = Math.min(-2.9999872E7D, dstWorld.getWorldBorder().minX() + 16.0D);
				double minZ = Math.min(-2.9999872E7D, dstWorld.getWorldBorder().minZ() + 16.0D);
				double maxX = Math.min(2.9999872E7D, dstWorld.getWorldBorder().maxX() - 16.0D);
				double maxZ = Math.min(2.9999872E7D, dstWorld.getWorldBorder().maxZ() - 16.0D);
				x = MathHelper.clamp(x, minX, maxX);
				z = MathHelper.clamp(z, minZ, maxZ);
				entity.setLocationAndAngles(x, y, z, yaw1, pitch);

				//Chance to spawn portal here

				srcWorld.getProfiler().endSection();
				entity.setWorld(dstWorld);
				dstWorld.addDuringPortalTeleport(player);
				player.connection.setPlayerLocation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), yaw1, pitch);
				return entity;
			} else {
				return null;
			}
		}
	};

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			World world, PlayerEntity player, Hand hand) {
		if (!player.isSecondaryUseActive()) {
			ItemStack stack = player.getHeldItem(hand);
			DimensionType type;
			if (world.getDimension().getType() != DimensionType.OVERWORLD) type = DimensionType.OVERWORLD;
			else type = ETDims.DARK_TYPE;
			if (player.changeDimension(type, TeleTools.TELEPORTER) != null)
				return ActionResult.resultSuccess(stack);
			return ActionResult.resultFail(stack);
		} else {
			int x = (int)player.getPosX();
			int z = (int)player.getPosZ();
			Chunk chunk = world.getChunk(x >> 4, z >> 4);
			chunk.getHeightmaps().forEach(e -> {
				player.sendStatusMessage(new StringTextComponent(String.format(
						"[%s] %s: %d",
						world.isRemote?"C":"S",
						e.getKey(),
						e.getValue().getHeight(x & 15, z & 15)
				)), false);
			});
			return super.onItemRightClick(world, player, hand);
		}
	}
}
