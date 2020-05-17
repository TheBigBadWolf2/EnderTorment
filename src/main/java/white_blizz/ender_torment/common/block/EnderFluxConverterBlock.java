package white_blizz.ender_torment.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import white_blizz.ender_torment.common.tile_entity.EnderFluxConverterTE;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.UnaryOperator;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnderFluxConverterBlock extends EnderFluxBlock {
	public EnderFluxConverterBlock() {
		super(UnaryOperator.identity());
	}

	@Override public boolean hasTileEntity(BlockState state) { return true; }

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EnderFluxConverterTE();
	}
}
