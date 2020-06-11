package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import white_blizz.ender_torment.common.tile_entity.CompactionTE;
import white_blizz.ender_torment.common.tile_entity.ETTileEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompactionBlock extends ETBlock {
	public CompactionBlock() {
		super(Properties.create(Material.IRON)
				.hardnessAndResistance(-1, 100000)
				.noDrops()
				.notSolid()
				.variableOpacity()
		);
	}

	@Override public BlockRenderType getRenderType(BlockState state) { return hasTileEntity(state) ? BlockRenderType.ENTITYBLOCK_ANIMATED : super.getRenderType(state); }
	@Override public boolean hasTileEntity(BlockState state) { return ETBlocks.COMPACTION_TYPE != null; }
	@Nullable @Override public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CompactionTE();
	}

	/*@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}*/

	@Override public boolean isVariableOpacity() { return true; }

	private <T> Optional<T> wrap(IBlockReader world, BlockPos pos, Function<BlockState, T> wrapper) {
		return ETTileEntity.get(CompactionTE.class, world, pos)
				.flatMap(CompactionTE::getState)
				.map(wrapper)
		;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return ETTileEntity.get(CompactionTE.class, worldIn, pos)
				.map(te -> te.getShape(1F))
				.map(VoxelShapes::create)
				.orElseGet(VoxelShapes::fullCube);
	}
}
