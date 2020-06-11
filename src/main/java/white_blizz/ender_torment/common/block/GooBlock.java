package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import white_blizz.ender_torment.utils.ETWorldUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GooBlock extends ETBlock {
	public GooBlock() { super(Properties.create(Material.CLAY)); }

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		double scale = 1;
		double xzScale = scale * 0.5;
		double yScale = scale * 0.15;
		entityIn.setMotion(entityIn.getMotion().mul(xzScale, yScale, xzScale));
	}

	@Override
	public boolean collisionExtendsVertically(BlockState state, IBlockReader world, BlockPos pos, Entity collidingEntity) {
		return false;
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.9D, 1.0D));
	}

	@Override public boolean isVariableOpacity() { return true; }

	@Override
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return super.getOpacity(state, worldIn, pos);
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return super.getRaytraceShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return super.getRenderShape(state, worldIn, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		int light = 0;
		if (worldIn instanceof ILightReader) light = ETWorldUtils.getLightAround(
				(ILightReader) worldIn, pos,
				ETWorldUtils.LightCombo.BLOCK,
				ETWorldUtils.CombineFunction.SUM,
				ETWorldUtils.CombineFunction.SUM
		);
		if (light == 0) return VoxelShapes.empty();
		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return super.getShape(state, worldIn, pos, context);
	}
}
