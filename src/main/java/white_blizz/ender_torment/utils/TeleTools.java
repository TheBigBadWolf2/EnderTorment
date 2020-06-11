package white_blizz.ender_torment.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

public final class TeleTools {
	private static final Map<Class<? extends Entity>, ITeleHelper<?>> map = new HashMap<>();

	public static <T extends Entity> void add(Class<T> clazz, ITeleHelper<T> helper) {
		map.put(clazz, helper);
	}

	static {
		add(ServerPlayerEntity.class, ((entity, srcWorld, dstWorld, yaw) -> {
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
			{
				/*AxisAlignedBB box = entity.getBoundingBox();
				if (dstWorld.hasNoCollisions(entity)) {
					do {
						box = box.offset(0, -1, 0);
					} while (dstWorld.hasNoCollisions(entity, box));
					y = box.minY + 1;
				} else {
					do {
						box = box.offset(0, 1, 0);
					} while (!dstWorld.hasNoCollisions(entity, box));
					y = box.minY;
				}*/
				y = ETWorldUtils.getHeight(dstWorld, Heightmap.Type.WORLD_SURFACE, x, z);
			}

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
			dstWorld.addDuringPortalTeleport(entity);
			entity.connection.setPlayerLocation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), yaw1, pitch);
			return entity;
		}));
	}

	private static class TeleInfo<T extends Entity> {
		private final T entity;
		private final ITeleHelper<T> helper;

		public TeleInfo(T entity, ITeleHelper<T> helper) {
			this.entity = entity;
			this.helper = helper;
		}

		public T tele(ServerWorld srcWorld, ServerWorld dstWorld, float yaw) {
			return helper.placeEntity(entity, srcWorld, dstWorld, yaw);
		}
	}

	@SuppressWarnings("unchecked") @Nullable
	private static <T extends Entity> TeleInfo<T> get(Entity entity) {
		Class<? extends Entity> entClass = entity.getClass();
		ITeleHelper<T> helper = null;
		while (helper == null) {
			ITeleHelper<?> helper1 = map.get(entClass);
			if (helper1 == null) {
				if (entClass != Entity.class) entClass = (Class<? extends Entity>)
						entClass.getSuperclass();
				else return null;
			} else helper = (ITeleHelper<T>) helper1;
		}
		return new TeleInfo<>((T)entity, helper);
	}

	public interface ITeleHelper<T extends Entity> {
		@Nullable T placeEntity(T entity, ServerWorld srcWorld, ServerWorld dstWorld, float yaw);
	}

	public static final ITeleporter TELEPORTER = new ITeleporter() {
		@Override
		public Entity placeEntity(
				Entity entity,
				ServerWorld srcWorld,
				ServerWorld dstWorld,
				float yaw, Function<Boolean, Entity> repositionEntity) {
			TeleInfo<? extends Entity> teleInfo = get(entity);
			if (teleInfo == null) return repositionEntity.apply(false);
			return teleInfo.tele(srcWorld, dstWorld, yaw);
		}
	};
}
