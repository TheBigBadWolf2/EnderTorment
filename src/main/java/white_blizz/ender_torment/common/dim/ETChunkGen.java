package white_blizz.ender_torment.common.dim;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ETChunkGen extends ChunkGenerator<ETChunkGen.Config> {

	public ETChunkGen(IWorld worldIn, BiomeProvider biomeProviderIn, Config generationSettingsIn) {
		super(worldIn, biomeProviderIn, generationSettingsIn);
	}

	@Override
	public void generateSurface(WorldGenRegion region, IChunk chunk) {
		ChunkPos chunkpos = chunk.getPos();
		int ccX = chunkpos.x;
		int ccZ = chunkpos.z;
		SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
		sharedseedrandom.setBaseChunkSeed(ccX, ccZ);
		ChunkPos chunkpos1 = chunk.getPos();
		int csX = chunkpos1.getXStart();
		int csZ = chunkpos1.getZStart();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for(int cX = 0; cX < 16; ++cX) {
			for(int cZ = 0; cZ < 16; ++cZ) {
				int x = csX + cX;
				int z = csZ + cZ;
				int y = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, cX, cZ) + 1;
				region.getBiome(pos.setPos(x, y, z)).buildSurface(sharedseedrandom, chunk, x, z, y, 0, this.getSettings().getDefaultBlock(), this.getSettings().getDefaultFluid(), this.getSeaLevel(), this.world.getSeed());
			}
		}
	}

	@Override
	public int getGroundHeight() {
		return world.getSeaLevel()+1;
	}

	@Override
	public void makeBase(IWorld world, IChunk chunk) {
		BlockState bedrock = Blocks.BEDROCK.getDefaultState();
		BlockState filler = getSettings().getDefaultBlock();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (pos.setX(0); pos.getX() < 16; pos.move(1, 0, 0)) {
			for (pos.setZ(0); pos.getZ() < 16; pos.move(0, 0, 1)) {
				pos.setY(0);
				chunk.setBlockState(pos, bedrock, false);
				for (pos.setY(1); pos.getY() < getGroundHeight(); pos.move(0, 1, 0)) {
					chunk.setBlockState(pos, filler, false);
				}
			}
		}
	}

	@Override
	public int func_222529_a(int x, int z, Heightmap.Type heightmapType) {
		if (heightmapType.isUsageNotWorldgen()) {
			int h = world.getMaxHeight();
			BlockPos.Mutable pos = new BlockPos.Mutable(x, h, z);
			for (; pos.getY() > 0; pos.move(0, -1, 0)) {
				if (!heightmapType.getHeightLimitPredicate().test(world.getBlockState(pos))) {
					return pos.getY();
				}
			}
			//return 0;
		}
		return getGroundHeight();
	}

	public static class Config extends GenerationSettings {

	}
}
