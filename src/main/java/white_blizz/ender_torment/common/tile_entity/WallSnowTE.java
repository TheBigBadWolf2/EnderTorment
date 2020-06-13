package white_blizz.ender_torment.common.tile_entity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import white_blizz.ender_torment.common.block.ETBlocks;
import white_blizz.ender_torment.common.math.IVec3f;
import white_blizz.ender_torment.common.math.MutVec3f;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WallSnowTE extends ETTileEntity {
	public static final ModelProperty<IVec3f[][][]> PROP = new ModelProperty<>();

	public static Optional<WallSnowTE> get(
			IBlockReader world, BlockPos pos) {
		return ETTileEntity.get(WallSnowTE.class, world, pos);
	}

	private int[] layers = new int[6];

	public WallSnowTE() { super(ETBlocks.WALL_SNOW_TYPE); }

	public void addLayer(Direction dir) {
		++layers[dir.getIndex()];
		markDirty();
		requestModelDataUpdate();
	}

	public int getLayers(Direction dir) {
		return layers[dir.getIndex()];
	}

	@Override
	protected void extraRead(CompoundNBT tag) {
		layers = tag.getIntArray("layers");
	}

	@Override
	protected void extraWrite(CompoundNBT tag) {
		tag.putIntArray("layers", layers);
	}

	@Override
	protected List<Cap<?>> getCaps() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public IModelData getModelData() {
		MutVec3f[][][] values = new MutVec3f[2][2][2];

		


		return new ModelDataMap.Builder().withInitial(PROP, values).build();
	}


}
