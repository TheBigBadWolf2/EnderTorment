package white_blizz.ender_torment.common.dim;

import com.mojang.datafixers.Dynamic;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ETSurfaceBuilder extends SurfaceBuilder<ETSurfaceBuilder.Config> {

	public ETSurfaceBuilder() {
		super(Config::deserialize);
	}

	@Override
	public void buildSurface(
			Random random, IChunk chunk, Biome biome,
			int x, int z, int startHeight,
			double noise, BlockState defaultBlock, BlockState defaultFluid,
			int seaLevel, long seed, Config config) {
		BlockState top = config.top;
		BlockState under = config.under;
		BlockPos.Mutable pos = new BlockPos.Mutable();
		int i = -1;
		int j = (int)(noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		int k = x & 15;
		int l = z & 15;

		for(int i1 = startHeight; i1 >= 0; --i1) {
			pos.setPos(k, i1, l);
			BlockState blockstate2 = chunk.getBlockState(pos);
			if (blockstate2.isAir()) {
				i = -1;
			} else if (blockstate2.getBlock() == defaultBlock.getBlock()) {
				if (i == -1) {
					if (j <= 0) {
						top = Blocks.AIR.getDefaultState();
						under = defaultBlock;
					} else if (i1 >= seaLevel - 4 && i1 <= seaLevel + 1) {
						top = config.top;
						under = config.under;
					}

					if (i1 < seaLevel && (top == null || top.isAir())) {
						if (biome.getTemperature(pos.setPos(x, i1, z)) < 0.15F) {
							top = Blocks.BLUE_ICE.getDefaultState();
						} else {
							top = defaultFluid;
						}

						pos.setPos(k, i1, l);
					}

					i = j;
					if (i1 >= seaLevel - 1) {
						chunk.setBlockState(pos, top, false);
					} else if (i1 < seaLevel - 7 - j) {
						top = Blocks.AIR.getDefaultState();
						under = defaultBlock;
						chunk.setBlockState(pos, config.underWater, false);
					} else {
						chunk.setBlockState(pos, under, false);
					}
				} else if (i > 0) {
					--i;
					chunk.setBlockState(pos, under, false);
					if (i == 0 && under.getBlock() == Blocks.SAND && j > 1) {
						i = random.nextInt(4) + Math.max(0, i1 - 63);
						under = under.getBlock() == Blocks.RED_SAND ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
					}
				}
			}
		}
	}

	public static class Config implements ISurfaceBuilderConfig {
		private final BlockState top, under, underWater;

		public Config(BlockState top, BlockState under, BlockState underWater) {
			this.top = top;
			this.under = under;
			this.underWater = underWater;
		}

		@Override public BlockState getTop() { return top; }
		@Override public BlockState getUnder() { return under; }
		public BlockState getUnderWater() { return underWater; }

		private static Config deserialize(Dynamic<?> dynamic) {
			BlockState top = dynamic.get("top_material").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
			BlockState under = dynamic.get("under_material").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
			BlockState underWater = dynamic.get("under_water_material").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
			return new Config(top, under, underWater);
		}
	}
}
