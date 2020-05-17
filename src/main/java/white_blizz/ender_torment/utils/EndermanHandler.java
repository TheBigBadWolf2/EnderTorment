package white_blizz.ender_torment.utils;

import net.minecraft.particles.IParticleData;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import white_blizz.ender_torment.EnderTorment;
import white_blizz.ender_torment.common.ender_flux.EnderFluxDamageSource;
import white_blizz.ender_torment.common.tile_entity.EnderFluxCollectorTE;

import java.util.Objects;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public final class EndermanHandler {
	@SubscribeEvent
	public static void teleport(EnderTeleportEvent event) {
		Vec3d start = event.getEntityLiving().getPositionVec();
		Vec3d end = new Vec3d(event.getTargetX(), event.getTargetY(), event.getTargetZ());
		Stream<EnderFluxCollectorTE> collectors = event.getEntityLiving().world.loadedTileEntityList
				.stream()
				.map(te -> {
					if (te instanceof EnderFluxCollectorTE)
						return (EnderFluxCollectorTE) te;
					return null;
				})
				.filter(Objects::nonNull)
				.filter(te -> te.inRange(start) || te.inRange(end));

		double dmg = collectors.reduce(0D, (i, te) -> {
			double s = Math.sqrt(te.getPos().distanceSq(start, true));
			double e = Math.sqrt(te.getPos().distanceSq(end, true));
			double d = start.distanceTo(end);

			EnderTorment.LOGGER.debug("Start: {} End: {} Dist: {}", s, e, d);

			double r = te.getRange();

			double pS = (r - s) / r;
			double pE = (r - e) / r;

			double amount = d;
			EnderTorment.LOGGER.debug("Starting power: {}", amount);
			if (pS < 0) {
				EnderTorment.LOGGER.debug("Lost: {}", pS * d);
				amount += pS * d;
			}
			if (pE < 0) {
				EnderTorment.LOGGER.debug("Lost: {}", pE * d);
				amount += pE * d;
			}
			if (amount < 1) {
				EnderTorment.LOGGER.debug("Teleport was too weak!");
				return 0D;
			}

			te.charge(MathHelper.floor(amount));
			return amount;
		}, Double::sum);
		if (dmg > 0) {
			//event.setCanceled(true);
			EnderTorment.LOGGER.debug("Teleport caught!");
			event.getEntityLiving().attackEntityFrom(
					new EnderFluxDamageSource(), (float) dmg
			);
			/*IParticleData data = new RedstoneParticleData(0, 0, 0, 1);

			Vec3d v = start;
			while (true) {
				event.getEntity().world.addParticle(data,
						v.x, v.y, v.z,
						0, 0,0);
				MathHelper.lerp2()
			}*/
		} else {
			EnderTorment.LOGGER.debug("Teleport not caught!");
		}
	}
}
