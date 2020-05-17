package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import white_blizz.ender_torment.common.ender_flux.CapabilityEnderFlux;
import white_blizz.ender_torment.common.ender_flux.IEnderFluxStorage;
import white_blizz.ender_torment.common.tile_entity.ETTileEntity;
import white_blizz.ender_torment.common.tile_entity.EnderFluxCollectorTE;
import white_blizz.ender_torment.utils.ETUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;
import java.util.function.UnaryOperator;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxCollectorBlock extends EnderFluxBlock {

	public EnderFluxCollectorBlock() {
		super(UnaryOperator.identity());
	}

	@Override public boolean hasTileEntity(BlockState state) { return true; }

	@Nullable @Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EnderFluxCollectorTE();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResultType onBlockActivated(
			BlockState state, World worldIn, BlockPos pos,
			PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

		Optional<EnderFluxCollectorTE> collector = ETTileEntity.get(EnderFluxCollectorTE.class, worldIn, pos);
		if (!collector.isPresent()) return ActionResultType.FAIL;
		player.sendStatusMessage(new StringTextComponent(String.format(
				"[%s] Flux: %d",
				worldIn.isRemote()?"C":"S",
				collector.get().getCapability(CapabilityEnderFlux.ENDER_FLUX).map(IEnderFluxStorage::getEnderFluxStored).orElse(-1)
		)), false);
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean ticksRandomly(BlockState state) {
		return super.ticksRandomly(state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		Vec3d offset = ETUtils.center(pos);
		IParticleData data = new RedstoneParticleData(1, 1, 1, 1);
		for (int i = 0; i < 100; i++) {
			double dist = rand.nextInt(16) + rand.nextDouble();
			double yaw = Math.toRadians(rand.nextInt(360) + rand.nextDouble());
			double pitch = Math.toRadians(rand.nextInt(180) + rand.nextDouble() - 90);

			Vec3d vec = new Vec3d(0, 0, dist)
					.rotatePitch((float) pitch)
					.rotateYaw((float) yaw)
					.add(offset);

			worldIn.addParticle(
					data,
					vec.x, vec.y, vec.z,
					0, 0, 0
			);
		}
	}


}