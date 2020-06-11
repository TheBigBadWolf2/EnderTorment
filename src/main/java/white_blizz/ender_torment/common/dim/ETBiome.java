package white_blizz.ender_torment.common.dim;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ETBiome extends Biome {
	public ETBiome() {
		super(new Builder()
				.surfaceBuilder(ETBiomes.BUILDER,
						new ETSurfaceBuilder.Config(Blocks.GRASS_BLOCK.getDefaultState(),
								Blocks.DIRT.getDefaultState(),
								Blocks.GRAVEL.getDefaultState()))
				.precipitation(RainType.SNOW)
				.category(Category.NONE)
				.depth(0.125F)
				.scale(1F)
				.downfall(0F)
				.waterColor(0x000000)
				.waterFogColor(0x000000)
				.temperature(0)

		);
	}

	@Override public int getSkyColor() { return 0x000000; }

	@Override
	public boolean doesSnowGenerate(IWorldReader worldIn, BlockPos pos) {
		if (this.getTemperature(pos) >= 0.15F) {
			return false;
		} else {
			if (pos.getY() >= 0 && pos.getY() < 256 && worldIn.getLightFor(LightType.BLOCK, pos) < 10) {
				BlockState blockstate = worldIn.getBlockState(pos);
				if ((blockstate.isAir(worldIn, pos) || blockstate.isReplaceable(Fluids.EMPTY)) && Blocks.SNOW.getDefaultState().isValidPosition(worldIn, pos))
					return true;
				if (blockstate.getBlock() == Blocks.SNOW)
					return true;
			}

			return false;
		}
	}
}
