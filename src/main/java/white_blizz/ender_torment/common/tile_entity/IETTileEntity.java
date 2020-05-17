package white_blizz.ender_torment.common.tile_entity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import white_blizz.ender_torment.utils.ETUtils;

import java.util.Optional;

public interface IETTileEntity extends IForgeTileEntity {

	static <T extends TileEntity & IETTileEntity> Optional<T> get(
			Class<T> clazz, IBlockReader world,
			BlockPos pos) {
		return ETUtils.getTileEntity(world, pos).map(te -> {
			if (clazz.isInstance(te)) return clazz.cast(te);
			return null;
		});
	}
}